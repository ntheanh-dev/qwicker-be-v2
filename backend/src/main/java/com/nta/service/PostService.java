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
import com.nta.model.Message;
import com.nta.repository.*;
import com.nta.service.websocker.LocationService;
import com.nta.service.websocker.OnlineOfflineService;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final ShipperPostService shipperPostService;

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

  @Async("taskExecutor")
  protected void handleFoundShippers(final Post post) {
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
    // update data in db
    postHistoryRepository.save(PostHistory.builder()
            .status(PostStatus.FOUND_SHIPPER)
            .post(post)
            .statusChangeDate(LocalDateTime.now())
            .build());

    sPost.setStatus(ShipperPostStatus.APPROVAL);
    shipperPostRepository.save(sPost);

    post.setStatus(PostStatus.FOUND_SHIPPER);
    final Post updatePost = postRepository.save(post);

    // notify found shipper to user, and joined shippers
    simpMessageSendingOperations.convertAndSend(
            "/topic/post/" + post.getId(),
            FoundShipperResponse.builder()
                    .shipper(shipperMapper.toShipperResponse(s))
                    .post(postMapper.toPostResponse(updatePost))
                    .messageType(MessageType.FOUND_SHIPPER)
                    .build());

    log.info("Found shipper: {}, for post: {}", s.getId(), post);
    // remove SHIPPER_RECEIVED_MSG on redis
    redisService.delete(RedisKey.SHIPPER_RECEIVED_MSG, post.getId());
  }

  public void pushDeliveryRequestToShipper(final String postId) {
    final Post post = findById(postId);
    if (post.getStatus() != PostStatus.PENDING) return;
    pushDeliveryRequestToShipper(post);
  }

  // Khởi động tác vụ định kỳ cho một Post cụ thể dựa trên postId
  public void pushDeliveryRequestToShipper(final Post post) {
    // Lên lịch tác vụ để chạy xử lý cho Post này
    ScheduledFuture<?> future =
        scheduler.scheduleAtFixedRate(() -> processPost(post), 0, 5, TimeUnit.SECONDS);
    scheduledTasks.put(post.getId(), future);
  }

  private void processPost(final Post post) {
    if (isValidPostToPushMsg(post)) { // Chua co shipper join post
      log.info("Processing post {}", post.getId());
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
                  log.info("Push request to shipper: {}", shipperId);
                  // Lưu lại những shipper da nhan msg
                  redisService.hashSet(RedisKey.SHIPPER_RECEIVED_MSG, post.getId(), shipperId);
                }
              });
    } else { // da qua 15s va co shipper join
      handleFoundShippers(post);
      stopScheduledTask(post.getId());
    }
  }

  private boolean isValidPostToPushMsg(final Post post) {
    if (Duration.between(post.getPostTime(), LocalDateTime.now()).toMinutes() <= 15) {
      return true;
    }
    // Neu chua co shipper join sau 15s => tiep tuc push
    return !shipperPostRepository.existsByPostId(post.getId());
  }

  private boolean isValidShipperToPushMsg(final String shipperId, final Post post) {
    Shipper s = shipperService.findById(shipperId);
    if (!onlineOfflineService.isUserSubscribed(
        s.getUser().getId(), "/topic/shipper/" + shipperId)) {
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

  @PreAuthorize("hasRole('SHIPPER')")
  public void joinPost(final String postId) { // for API
    final Post p =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    if (p.getStatus() == PostStatus.PENDING) {
      final Shipper s = shipperService.getCurrentShipper();
      final Optional<ShipperPost> shipperPost =
          shipperPostRepository.findByShipperIdAndPostId(s.getId(), postId);
      if (shipperPost.isPresent()) {
        throw new AppException(ErrorCode.SHIPPER_POST_EXISTED);
      } else {
        ShipperPost newShipperPost =
            ShipperPost.builder()
                .shipper(s)
                .joinedAt(LocalDateTime.now())
                .status(ShipperPostStatus.JOINED)
                .post(p)
                .build();
        shipperPostRepository.save(newShipperPost);
        //-------Update num joined shipper
        simpMessageSendingOperations.convertAndSend(
            "/topic/post/" + postId,
            Message.builder().content(
                    String.valueOf(shipperPostService.countByPostId(postId))
            ).messageType(MessageType.NUM_SHIPPER_JOINED).build());
      }
    } else {
      throw new AppException(ErrorCode.POST_WAS_TAKEN);
    }
  }

  //NOTE: for websocket
  public void joinPost(final String postId, final String shipperId) {
    final Post p =
            postRepository
                    .findById(postId)
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    if (p.getStatus() == PostStatus.PENDING) {
      final Shipper s = shipperService.findById(shipperId);
      final Optional<ShipperPost> shipperPost =
              shipperPostRepository.findByShipperIdAndPostId(s.getId(), postId);
      if (shipperPost.isPresent()) {
        throw new AppException(ErrorCode.SHIPPER_POST_EXISTED);
      } else {
        ShipperPost newShipperPost =
                ShipperPost.builder()
                        .shipper(s)
                        .joinedAt(LocalDateTime.now())
                        .status(ShipperPostStatus.JOINED)
                        .post(p)
                        .build();
        shipperPostRepository.save(newShipperPost);
        //-------Update num joined shipper
        simpMessageSendingOperations.convertAndSend(
                "/topic/post/" + postId,
                Message.builder().content(
                        String.valueOf(shipperPostService.countByPostId(postId))
                ).messageType(MessageType.NUM_SHIPPER_JOINED).build());
        log.info("Shipper: {}, joined post: {}",shipperId,postId);
      }
    } else {
      simpMessageSendingOperations.convertAndSend(
              "/topic/post/" + postId,
              Message.builder().messageType(MessageType.POST_WAS_TAKEN).build());
    }
  }

  // Dừng tất cả tác vụ khi ứng dụng kết thúc
  @PreDestroy
  public void destroy() {
    scheduler.shutdown();
    scheduledTasks.forEach((postId, future) -> future.cancel(true));
  }
}
