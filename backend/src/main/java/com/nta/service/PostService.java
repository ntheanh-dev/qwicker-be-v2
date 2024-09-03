package com.nta.service;

import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.ws.FoundShipperResponse;
import com.nta.entity.*;
import com.nta.enums.ErrorCode;
import com.nta.enums.MessageType;
import com.nta.enums.PostStatus;
import com.nta.enums.ShipperPostStatus;
import com.nta.exception.AppException;
import com.nta.mapper.LocationMapper;
import com.nta.mapper.PostMapper;
import com.nta.mapper.ProductMapper;
import com.nta.dto.request.ws.DeliveryRequest;
import com.nta.mapper.ShipperMapper;
import com.nta.repository.*;
import com.nta.service.websocker.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostService {
    ProductMapper productMapper;
    LocationMapper locationMapper;
    SimpMessageSendingOperations simpMessageSendingOperations;
    CloudinaryService cloudinaryService;
    ProductCategoryRepository productCategoryRepository;
    ProductRepository productRepository;
    LocationRepository locationRepository;
    PaymentMethodRepository paymentMethodRepository;
    VehicleRepository vehicleRepository;
    PostRepository postRepository;
    PostHistoryRepository postHistoryRepository;
    PaymentRepository paymentRepository;
    UserService userService;
    LocationService locationService;
    PostMapper postMapper;
    ShipperPostRepository shipperPostRepository;
    ShipperService shipperService;
    ShipperMapper shipperMapper;

    @Transactional
    public Post createPost(final PostCreationRequest request) {
        //--------------Product-----------------
        final Product prod = productMapper.toProduct(request.getProduct());
        prod.setImage(cloudinaryService.url(request.getProduct().getFile()));
        ProductCategory prodCate = productCategoryRepository.findById(request.getProduct().getCategoryId()).orElseThrow(
                () -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)
        );
        prod.setCategory(prodCate);
        final Product createdProd = productRepository.save(prod);
        //--------------Location-----------------
        final Location pickup = locationRepository.save(locationMapper.toLocation(request.getShipment().getPickupLocation()));
        final Location drop = locationRepository.save(locationMapper.toLocation(request.getShipment().getDropLocation()));
        //-------------Payment-------------------
        final PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getMethod()).orElseThrow(
                () -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)
        );
        final Payment payment = Payment.builder()
                .isPosterPay(request.getPayment().isPosterPay())
                .price(request.getShipment().getCost())
                .method(paymentMethod)
                .build();
        //-----------Post--------------
        final Post post = Post.builder()
                .user(userService.currentUser())
                .description(request.getOrder().getDescription())
                .dropLocation(drop)
                .pickupLocation(pickup)
                .product(createdProd)
                .requestType(request.getShipment().getType())
                .vehicleType(vehicleRepository.findById(request.getOrder().getVehicleId()).orElseThrow(
                        () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND)
                ))
                .postTime(LocalDateTime.now())
                .payment(paymentRepository.save(payment))
                .status(
                        paymentMethod.getName().equals("Tiền Mặt") ? PostStatus.PENDING : PostStatus.WAITING_PAY
                )
                .build();
        final Post createdPost = postRepository.save(post);
        //---------------Post History----------------
        final PostHistory postHistory = new PostHistory();
        postHistory.setPost(createdPost);
        postHistory.setStatus(PostStatus.PENDING);
        postHistory.setStatusChangeDate(LocalDateTime.now());
        postHistoryRepository.save(postHistory);

        //--------------Find nearest shippers by async method---------
        pushDeliveryRequestToShipper(createdPost);
        return createdPost;
    }

    public Post findById(final String id) {
        return postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    public List<Post> getPostsByLatestStatus(final String statusList) {
        final var user = userService.currentUser();
        if (statusList == null || statusList.isEmpty()) {
            return postRepository.findPostsByUserId(user.getId());
        }
        final List<PostStatus> statusEnumList = Arrays.stream(statusList.split(","))
                .map(this::convertToEnum)
                .toList();
        return postRepository.findPostsByStatus(user.getId(), statusEnumList);
    }

    public Post getPostByStatus(final String status, final String postId) {
        final PostStatus postStatus = convertToEnum(status);
        return postRepository.findPostByIdAndStatus(postId, postStatus)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    public Post updatePostStatus(
            final String newStatus,
            final String postId,
            final String photo,
            final String description
    ) {
        final Post post = postRepository.findById(postId).orElse(null);
        final PostStatus newPostStatus = convertToEnum(newStatus);
        assert post != null;
        final PostStatus oldPostStatus = post.getStatus();
        if(newPostStatus == oldPostStatus) return post;
        verifyBeforeChangeStatus(oldPostStatus, newPostStatus); // will throw error if verify fail

        final PostHistory postHistory = PostHistory.builder()
                .post(post)
                .status(newPostStatus)
                .statusChangeDate(LocalDateTime.now())
                .build();
        if (photo != null) {
//            postHistory.setPhoto(cloudinaryService.url(photo));
        }
        if (description != null) {
            postHistory.setDescription(description);
        }
        postHistoryRepository.save(postHistory);
        post.setStatus(newPostStatus);
        return postRepository.save(post);
    }

    private void verifyBeforeChangeStatus(final PostStatus oldPostStatus, final PostStatus newPostStatus) {
        if (newPostStatus == PostStatus.CONFIRM_WITH_CUSTOMER && oldPostStatus != PostStatus.FOUND_SHIPPER) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_POST_STATUS);
        }
        if (newPostStatus == PostStatus.SHIPPED && oldPostStatus != PostStatus.CONFIRM_WITH_CUSTOMER) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_POST_STATUS);
        }
        if (newPostStatus == PostStatus.DELIVERED && oldPostStatus != PostStatus.SHIPPED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_POST_STATUS);
        }
    }

    private PostStatus convertToEnum(final String status) {
        try {
            return PostStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_POST_STATUS);
        }
    }

    @Async("taskExecutor")
    public void pushDeliveryRequestToShipper(final String postId) {
        final Post post = findById(postId);
        pushDeliveryRequestToShipper(post);
    }

    @Async("taskExecutor")
    public void pushDeliveryRequestToShipper(final Post post) {
        final Set<String> shipperId = locationService.getShippersByGeoHash(
                post.getPickupLocation().getLatitude(),
                post.getPickupLocation().getLongitude()
        );
        shipperId.forEach(s -> {
            var body = DeliveryRequest.builder()
                    .post(postMapper.toPostResponse(post))
                    .messageType(MessageType.DELIVERY_REQUEST)
                    .build();
            simpMessageSendingOperations.convertAndSend("/topic/shipper/" + s, body);
            log.info("delivery request to shipper: " + s);
        });
    }

    public void handleDeliveryRequest(final String postId) {
        final List<ShipperPost> shipperPosts = shipperPostRepository.findAllByPostId(postId);
        if (shipperPosts.isEmpty()) { // push message to shippers
            pushDeliveryRequestToShipper(postId);
        } else { // find nearest shippers
            if (shipperPosts.size() == 1) {
                final ShipperPost sPost = shipperPosts.getFirst();
                final Shipper s = sPost.getShipper();
                final Post post = sPost.getPost();
                // notify found shipper to user
                simpMessageSendingOperations.convertAndSend("/topic/post/" + postId,
                        FoundShipperResponse.builder()
                                .shipper(shipperMapper.toShipperResponse(s))
                                .messageType(MessageType.FOUND_SHIPPER)
                                .build()
                );
                // update data in db
                sPost.setStatus(ShipperPostStatus.APPROVAL);
                shipperPostRepository.save(sPost);

                post.setStatus(PostStatus.FOUND_SHIPPER);
                postRepository.save(post);

                // notify approval to shipper
                simpMessageSendingOperations.convertAndSend("/topic/shipper/" + s.getId(),
                        DeliveryRequest.builder()
                                .post(postMapper.toPostResponse(post))
                                .messageType(MessageType.APPROVED_SHIPPER)
                                .build()
                );
            } else {
                ////
            }
        }
    }

    @PreAuthorize("hasRole('SHIPPER')")
    public void joinPost(final String postId) {
        final Post p = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        if (p.getStatus() == PostStatus.PENDING) {
            final Shipper s = shipperService.getCurrentShipper();
            final Optional<ShipperPost> shipperPost = shipperPostRepository.findByShipperIdAndPostId(s.getId(), postId);
            if (shipperPost.isPresent()) {
                throw new AppException(ErrorCode.SHIPPER_POST_EXISTED);
            } else {
                ShipperPost newShipperPost = ShipperPost.builder()
                        .shipper(s)
                        .joinedAt(LocalDateTime.now())
                        .status(ShipperPostStatus.JOINED)
                        .post(p)
                        .build();
                shipperPostRepository.save(newShipperPost);
            }
        } else {
            throw new AppException(ErrorCode.POST_WAS_TAKEN);
        }
    }

}
