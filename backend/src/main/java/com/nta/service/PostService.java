package com.nta.service;

import com.nta.dto.request.post.PostCreationRequest;
import com.nta.entity.*;
import com.nta.enums.ErrorCode;
import com.nta.enums.PostStatus;
import com.nta.exception.AppException;
import com.nta.mapper.LocationMapper;
import com.nta.mapper.ProductMapper;
import com.nta.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    ProductMapper productMapper;
    LocationMapper locationMapper;

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
                .build();
        Post createdPost = postRepository.save(post);
        //---------------Post History----------------
        PostHistory postHistory = new PostHistory();
        postHistory.setPost(createdPost);
        postHistory.setStatus(PostStatus.PENDING);
        postHistory.setStatusChangeDate(LocalDateTime.now());
        postHistoryRepository.save(postHistory);

        return createdPost;
    }

    public Post findById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    public List<Post> findPostsByUserId() {
        var user = userService.currentUser();
        return postRepository.findPostsByUserId(user.getId());
    }

    public List<Post> getPostsByLatestStatus(PostStatus status) {
        var user = userService.currentUser();
        return postRepository.findPostsByLatestStatus(user.getId(),status);
    }
}
