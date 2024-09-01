package com.nta.service;

import com.nta.dto.request.post.PostCreationRequest;
import com.nta.entity.*;
import com.nta.enums.ErrorCode;
import com.nta.enums.MessageType;
import com.nta.enums.PostStatus;
import com.nta.exception.AppException;
import com.nta.mapper.LocationMapper;
import com.nta.mapper.PostMapper;
import com.nta.mapper.ProductMapper;
import com.nta.model.websocket.DeliveryRequest;
import com.nta.repository.*;
import com.nta.service.websocker.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    @Transactional
    public Post createPost(PostCreationRequest request) {
        //--------------Product-----------------
        Product prod = productMapper.toProduct(request.getProduct());
        String url = cloudinaryService.url(request.getProduct().getFile());
        prod.setImage(url);
        ProductCategory prodCate = productCategoryRepository.findById(request.getProduct().getCategoryId()).orElseThrow(
                () -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)
        );
        prod.setCategory(prodCate);
        Product createdProd = productRepository.save(prod);
        //--------------Location-----------------
        Location pickup = locationRepository.save(locationMapper.toLocation(request.getShipment().getPickupLocation()));
        Location drop = locationRepository.save(locationMapper.toLocation(request.getShipment().getDropLocation()));
        //-------------Payment-------------------
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getMethod()).orElseThrow(
                () -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)
        );
        Payment payment = Payment.builder()
                .isPosterPay(request.getPayment().isPosterPay())
                .price(request.getShipment().getCost())
                .method(paymentMethod)
                .build();
        //-----------Post--------------
        Vehicle vehicle = vehicleRepository.findById(request.getOrder().getVehicleId()).orElseThrow(
                () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND)
        );
        Post post = Post.builder()
                .user(userService.currentUser())
                .description(request.getOrder().getDescription())
                .dropLocation(drop)
                .pickupLocation(pickup)
                .product(createdProd)
                .requestType(request.getShipment().getType())
                .vehicleType(vehicle)
                .postTime(LocalDateTime.now())
                .payment(paymentRepository.save(payment))
                .status(PostStatus.PENDING)
                .build();
        Post createdPost = postRepository.save(post);
        //---------------Post History----------------
        PostHistory postHistory = new PostHistory();
        postHistory.setPost(createdPost);
        postHistory.setStatus(PostStatus.PENDING);
        postHistory.setStatusChangeDate(LocalDateTime.now());
        postHistoryRepository.save(postHistory);

        //--------------Find nearest shippers by async method---------
        pushDeliveryRequestToShipper(createdPost);
        return createdPost;
    }

    public Post findById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    public List<Post> getPostsByLatestStatus(PostStatus status) {
        var user = userService.currentUser();
        return postRepository.findPostsByLatestStatus(user.getId(), status);
    }

    public List<Post> getPosts(String statusList) {
        var user = userService.currentUser();
        if (statusList == null || statusList.isEmpty()) {
            return postRepository.findPostsByUserId(user.getId());
        }
        List<PostStatus> statusEnumList = Arrays.stream(statusList.split(","))
                .map(this::convertToEnum)
                .toList();
        return postRepository.findPostsByStatus(user.getId(), statusEnumList);
    }

    private PostStatus convertToEnum(String status) {
        return PostStatus.valueOf(status.toUpperCase());
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

}
