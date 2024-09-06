package com.nta.service;

import com.nta.constant.RedisKey;
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
import com.nta.service.websocker.OnlineOfflineService;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

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

  OnlineOfflineService onlineOfflineService;

  ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
  Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
  RedisService redisService;

  private final GeoHashService geoHashService;

  @Transactional
  public Post createPost(final PostCreationRequest request) {
    // --------------Product-----------------
    final Product prod = productMapper.toProduct(request.getProduct());
    prod.setImage(cloudinaryService.url(request.getProduct().getFile()));
    ProductCategory prodCate =
        productCategoryRepository
            .findById(request.getProduct().getCategoryId())
            .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    prod.setCategory(prodCate);
    final Product createdProd = productRepository.save(prod);
    // --------------Location-----------------
    final Location pickup =
        locationRepository.save(
            locationMapper.toLocation(request.getShipment().getPickupLocation()));
    final Location drop =
        locationRepository.save(locationMapper.toLocation(request.getShipment().getDropLocation()));
    // -------------Payment-------------------
    final PaymentMethod paymentMethod =
        paymentMethodRepository
            .findById(request.getPayment().getMethod())
            .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
    final Payment payment =
        Payment.builder()
            .isPosterPay(request.getPayment().isPosterPay())
            .price(request.getShipment().getCost())
            .method(paymentMethod)
            .build();
    // -----------Post--------------
    final Post post =
        Post.builder()
            .user(userService.currentUser())
            .description(request.getOrder().getDescription())
            .dropLocation(drop)
            .pickupLocation(pickup)
            .product(createdProd)
            .requestType(request.getShipment().getType())
            .vehicleType(
                vehicleRepository
                    .findById(request.getOrder().getVehicleId())
                    .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND)))
            .postTime(LocalDateTime.now())
            .payment(paymentRepository.save(payment))
            .status(
                paymentMethod.getName().equals("Tiền Mặt")
                    ? PostStatus.PENDING
                    : PostStatus.WAITING_PAY)
            .build();
    final Post createdPost = postRepository.save(post);
    // ---------------Post History----------------
    final PostHistory postHistory = new PostHistory();
    postHistory.setPost(createdPost);
    postHistory.setStatus(PostStatus.PENDING);
    postHistory.setStatusChangeDate(LocalDateTime.now());
    postHistoryRepository.save(postHistory);

    // --------------Find nearest shippers by async method---------
    pushDeliveryRequestToShipper(createdPost);
    return createdPost;
  }

  public Post findById(final String id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
  }

  public List<Post> getPostsByLatestStatus(final String statusList) {
    final var user = userService.currentUser();
    if (statusList == null || statusList.isEmpty()) {
      return postRepository.findPostsByUserId(user.getId());
    }
    final List<PostStatus> statusEnumList =
        Arrays.stream(statusList.split(",")).map(this::convertToEnum).toList();
    return postRepository.findPostsByStatus(user.getId(), statusEnumList);
  }

  public Post getPostByStatus(final String status, final String postId) {
    final PostStatus postStatus = convertToEnum(status);
    return postRepository
        .findPostByIdAndStatus(postId, postStatus)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
  }

  public Post updatePostStatus(
      final String newStatus, final String postId, final String photo, final String description) {
    final Post post = postRepository.findById(postId).orElse(null);
    final PostStatus newPostStatus = convertToEnum(newStatus);
    assert post != null;
    final PostStatus oldPostStatus = post.getStatus();
    if (newPostStatus == oldPostStatus) return post;
    verifyBeforeChangeStatus(oldPostStatus, newPostStatus); // will throw error if verify fail

    final PostHistory postHistory =
        PostHistory.builder()
            .post(post)
            .status(newPostStatus)
            .statusChangeDate(LocalDateTime.now())
            .build();
    if (photo != null) {
      postHistory.setPhoto(cloudinaryService.url(photo));
    }
    if (description != null) {
      postHistory.setDescription(description);
    }
    postHistoryRepository.save(postHistory);
    post.setStatus(newPostStatus);
    return postRepository.save(post);
  }

  private void verifyBeforeChangeStatus(
      final PostStatus oldPostStatus, final PostStatus newPostStatus) {
    if (newPostStatus == PostStatus.CONFIRM_WITH_CUSTOMER
        && oldPostStatus != PostStatus.FOUND_SHIPPER) {
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

  public void handleFoundShipper(final Post post) {
    final List<ShipperPost> shipperPosts = shipperPostRepository.findAllByPostId(post.getId());
    final ShipperPost sPost = shipperPosts.getFirst();
    Shipper s = null;
    if (shipperPosts.size() == 1) {
      s = sPost.getShipper();
    } else { // find nearest shipper
      List<String> shipperIds = shipperPosts.stream().map(ShipperPost::getId).toList();
      String shipperId =
          geoHashService.findNearestShipperId(
              shipperIds,
              post.getId(),
              post.getPickupLocation().getLatitude(),
              post.getPickupLocation().getLongitude(),
              999);
      s = shipperService.findById(shipperId);
    }
    // notify found shipper to user
    simpMessageSendingOperations.convertAndSend(
        "/topic/post/" + post.getId(),
        FoundShipperResponse.builder()
            .shipper(shipperMapper.toShipperResponse(s))
            .messageType(MessageType.FOUND_SHIPPER)
            .build());
    // update data in db
    sPost.setStatus(ShipperPostStatus.APPROVAL);
    shipperPostRepository.save(sPost);

    post.setStatus(PostStatus.FOUND_SHIPPER);
    postRepository.save(post);

    // notify approval to shipper
    simpMessageSendingOperations.convertAndSend(
        "/topic/shipper/" + s.getId(),
        DeliveryRequest.builder()
            .post(postMapper.toPostResponse(post))
            .messageType(MessageType.APPROVED_SHIPPER)
            .build());

    // remove SHIPPER_RECEIVED_MSG on redis
    redisService.delete(RedisKey.SHIPPER_RECEIVED_MSG, post.getId());
  }

  public void pushDeliveryRequestToShipper(final String postId) {
    final Post post = findById(postId);
    pushDeliveryRequestToShipper(post);
  }

  // Khởi động tác vụ định kỳ cho một Post cụ thể dựa trên postId
  public void pushDeliveryRequestToShipper(final Post post) {
    // Lên lịch tác vụ để chạy xử lý cho Post này
    ScheduledFuture<?> future =
        scheduler.scheduleAtFixedRate(() -> processPost(post), 0, 5, TimeUnit.SECONDS);
    scheduledTasks.put(post.getId(), future);
  }

  // Xử lý Post dựa trên postId
  private void processPost(final Post post) {
    if (isValidPostToPushMsg(post)) { // Chua cho shipper join post
      geoHashService
          .getShipperDetailCacheByGeoHash(
              post.getPickupLocation().getLatitude(), post.getPickupLocation().getLongitude())
          .keySet()
          .forEach(
              shipperId -> {
                if (isValidShipperToPushMsg(shipperId, post)) {
                  var body =
                      DeliveryRequest.builder()
                          .post(postMapper.toPostResponse(post))
                          .messageType(MessageType.DELIVERY_REQUEST)
                          .build();
                  simpMessageSendingOperations.convertAndSend("/topic/shipper/" + shipperId, body);
                  log.info("delivery request to shipper: " + shipperId);
                  // Lưu lại những shipper da nhan msg
                  redisService.hashSet(RedisKey.SHIPPER_RECEIVED_MSG, post.getId(), shipperId);
                }
              });
    } else { // da tra qua 15s va co shipper join
      handleFoundShipper(post);
      stopScheduledTask(post.getId());
    }
  }

  // Kiểm tra Post có còn hợp lệ để tiếp tục xử lý không
  private boolean isValidPostToPushMsg(final Post post) {
    if (!shipperPostRepository.existsByPostId(post.getId())) {
      return true;
    }
    return Duration.between(post.getPostTime(), LocalDateTime.now()).toMinutes() >= 15;
  }

  private boolean isValidShipperToPushMsg(final String shipperId, final Post post) {
    Shipper s = shipperService.findById(shipperId);
    if (!onlineOfflineService.isUserSubscribed(
        s.getUser().getUsername(), "/topic/shipper/" + shipperId)) {
      return false;
    }
    // Neu da nhan duoc roi thi khong gui nua
    return !redisService.checkKeyFieldValueExists(
        RedisKey.SHIPPER_RECEIVED_MSG, post.getId(), shipperId);
  }

  // Dừng và hủy tác vụ cho một Post cụ thể
  private void stopScheduledTask(String postId) {
    ScheduledFuture<?> future = scheduledTasks.get(postId);
    if (future != null) {
      future.cancel(true);
      scheduledTasks.remove(postId);
      System.out.println("Tác vụ đã bị dừng cho Post: " + postId);
    }
  }

  // Dừng tất cả tác vụ khi ứng dụng kết thúc
  @PreDestroy
  public void destroy() {
    scheduler.shutdown();
    scheduledTasks.forEach((postId, future) -> future.cancel(true));
  }
}
