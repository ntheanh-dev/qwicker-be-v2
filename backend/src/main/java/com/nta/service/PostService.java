package com.nta.service;

import com.nta.constant.RedisKey;
import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.PaymentResponse;
import com.nta.dto.response.ws.FoundShipperResponse;
import com.nta.dto.response.ws.ShipperLocationResponse;
import com.nta.entity.*;
import com.nta.enums.ErrorCode;
import com.nta.enums.MessageType;
import com.nta.enums.PostStatus;
import com.nta.enums.ShipperPostStatus;
import com.nta.exception.AppException;
import com.nta.mapper.*;
import com.nta.dto.request.ws.DeliveryRequest;
import com.nta.model.Message;
import com.nta.model.ShipperDetailCache;
import com.nta.repository.*;
import com.nta.service.websocker.LocationService;
import com.nta.service.websocker.OnlineOfflineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
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
  PostMapper postMapper;
  ShipperPostRepository shipperPostRepository;
  ShipperService shipperService;
  ShipperMapper shipperMapper;
  OnlineOfflineService onlineOfflineService;
  PaymentMapper paymentMapper;
  List<String> runningPost = new ArrayList<>();
  RedisService redisService;
  GeoHashService geoHashService;
  ShipperPostService shipperPostService;
  LocationService locationService;

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
    // -------------Payment method-------------------
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
            .payment(paymentRepository.save(payment))
            .vehicleType(
                vehicleRepository
                    .findById(request.getOrder().getVehicleId())
                    .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND)))
            .postTime(LocalDateTime.now())
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
    payment.setPost(createdPost);
    paymentRepository.save(payment);
    if (paymentMethod.getName().equals("Tiền Mặt")) {
      // --------------Find nearest shippers by async method---------
      startPost(createdPost);
    }
    return createdPost;
  }

  public Post findById(final String id) {
    return postRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
  }

  public List<Post> getPostsByStatusList(final String statusList) {
    final var user = userService.currentUser();
    if (statusList == null || statusList.isEmpty()) {
      return postRepository.findPostsByUserId(user.getId());
    }
    final List<PostStatus> statusEnumList =
        Arrays.stream(statusList.split(",")).map(this::convertToEnum).toList();
    return postRepository.findPostsByStatus(user.getId(), statusEnumList);
  }

  public List<Post> getShipperPostsByStatusList(final String statusList) {
    final String shipperId = shipperService.getCurrentShipper().getId();
    log.info("originalStatusList: {}", statusList);

    final List<PostStatus> statusEnumList =
        Arrays.stream(statusList.split(",")).map(this::convertToEnum).toList();
    return postRepository.findPostsByStatusAndShipperId(
        shipperId, ShipperPostStatus.APPROVAL, statusEnumList);
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
    if (newPostStatus.equals(PostStatus.SHIPPED)) {
      post.setPickupDatetime(LocalDateTime.now());
    } else if (newPostStatus.equals(PostStatus.DELIVERED)) {
      post.setDropDateTime(LocalDateTime.now());
    }
    post.setStatus(newPostStatus);

    simpMessageSendingOperations.convertAndSend(
        "/topic/post/" + postId,
        Message.builder()
            .content(newPostStatus.name())
            .messageType(MessageType.UPDATE_POST_STATUS)
            .build());

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
    log.info("status: {}", status);
    try {
      return PostStatus.valueOf(status);
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.INVALID_POST_STATUS);
    }
  }

  public void selectShipperToShip(final Post post) {
    final List<ShipperPost> shipperPosts = shipperPostRepository.findAllByPostId(post.getId());
    ShipperPost sPost;
    Shipper s;
    if (shipperPosts.size() == 1) { // only one shipper joined
      sPost = shipperPosts.getFirst();
      s = sPost.getShipper();
    } else { // find nearest shipper
      log.info("Found more than one shipper in this region, trying to find nearest shipper.");
      List<String> shipperIds = shipperPosts.stream().map(ShipperPost::getId).toList();
      String shipperId =
          geoHashService.findNearestShipperId(
              shipperIds,
              post.getId(),
              post.getPickupLocation().getLatitude(),
              post.getPickupLocation().getLongitude(),
              999);
      s = shipperService.findById(shipperId);
      sPost = shipperPostRepository.findByPostIdAndShipperId(post.getId(), shipperId);
    }

    log.info("Shipper {} was approval to take {} post", s.getId(), post.getId());
    try {
      // notify found shipper to user, and joined shippers
      post.setStatus(PostStatus.FOUND_SHIPPER);
      simpMessageSendingOperations.convertAndSend(
          "/topic/post/" + post.getId(),
          FoundShipperResponse.builder()
              .shipper(shipperMapper.toShipperResponse(s))
              .post(postMapper.toPostResponse(postRepository.save(post)))
              .messageType(MessageType.FOUND_SHIPPER)
              .build());
      // update data in db
      postHistoryRepository.save(
          PostHistory.builder()
              .status(PostStatus.FOUND_SHIPPER)
              .post(post)
              .statusChangeDate(LocalDateTime.now())
              .build());
      sPost.setStatus(ShipperPostStatus.APPROVAL);
      shipperPostRepository.save(sPost);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    // remove SHIPPER_RECEIVED_MSG on redis
    redisService.delete(RedisKey.SHIPPER_RECEIVED_MSG, post.getId());
  }

  public void pushDeliveryRequestToShipper(final String postId) {
    final Post post = findById(postId);
    if (post.getStatus() != PostStatus.PENDING) return;
    startPost(post);
  }

  public void startPost(final Post post) {
    runningPost.add(post.getId());
  }

  @Scheduled(fixedRate = 10000)
  public void pushDeliveryRequestToShipper() {
    log.info("pushDeliveryRequestToShipper");
    if (runningPost.isEmpty()) return;
    final List<String> toRemove = new ArrayList<>();
    runningPost.forEach(
        id -> {
          final Post post = findById(id);
          if (post.getStatus() == PostStatus.PENDING) {
            if (isValidPostToPushMsg(post)) { // Chua co shipper join post
              log.info("Processing post {}", post.getId());
              geoHashService
                      .getShippersDetailCacheByGeoHash(
                              post.getPickupLocation().getLatitude(), post.getPickupLocation().getLongitude())
                      .keySet()
                      .forEach(
                              shipperId -> {
                                log.info("Found shipper: {}", shipperId);
                                if (isValidShipperToPushMsg(shipperId, post.getId())) {
                                  try {
                                    log.info("Pushing delivery request to shipper: {}", shipperId);
                                    simpMessageSendingOperations.convertAndSend(
                                            "/topic/shipper/" + shipperId,
                                            DeliveryRequest.builder()
                                                    .post(postMapper.toPostResponse(post))
                                                    .messageType(MessageType.DELIVERY_REQUEST)
                                                    .build());
                                    // Lưu lại những shipper da nhan msg
                                    redisService.hashSet(
                                            RedisKey.SHIPPER_RECEIVED_MSG, post.getId(), shipperId);
                                  } catch (Exception e) {
                                    log.error(e.getMessage());
                                  }
                                }
                              });
            } else { // da qua 15s va co shipper join
              selectShipperToShip(post);
            }
          } else {
            toRemove.add(post.getId());
          }
        });
    runningPost.removeAll(toRemove);
    toRemove.clear();
  }

  private boolean isValidPostToPushMsg(final Post post) {
    if (Duration.between(post.getPostTime(), LocalDateTime.now()).toSeconds() <= 10) {
      return true;
    }
    // Neu chua co shipper join sau 10s => tiep tuc push
    final var hasShipperJoined = shipperPostRepository.existsByPostId(post.getId());
    log.info("hasShipperJoined: {}", hasShipperJoined);
    return !hasShipperJoined;
  }

  private boolean isValidShipperToPushMsg(final String shipperId, final String postId) {
    Shipper s = shipperService.findById(shipperId);
    if (!onlineOfflineService.isUserSubscribed(
        s.getUser().getId(), "/topic/shipper/" + shipperId)) {
      return false;
    }
    // Neu da nhan duoc roi thi khong gui nua
    return !redisService.checkKeyFieldValueExists(RedisKey.SHIPPER_RECEIVED_MSG, postId, shipperId);
  }
  // NOTE: for websocket
  public void joinPost(final String postId, final String shipperId) {
    final Post p =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    if (p.getStatus() == PostStatus.PENDING) {
      final Shipper s = shipperService.findById(shipperId);
      final Optional<ShipperPost> shipperPost =
          shipperPostRepository.findByShipperIdAndPostId(s.getId(), postId);
      if (shipperPost.isEmpty()) {
        shipperPostRepository.save(
            ShipperPost.builder()
                .shipper(s)
                .joinedAt(LocalDateTime.now())
                .status(ShipperPostStatus.JOINED)
                .post(p)
                .build());
        // -------Update num joined shipper
        simpMessageSendingOperations.convertAndSend(
            "/topic/post/" + postId,
            Message.builder()
                .content(String.valueOf(shipperPostService.countByPostId(postId)))
                .messageType(MessageType.NUM_SHIPPER_JOINED)
                .build());
        log.info("Shipper: {}, joined post: {}", shipperId, postId);
      }
    } else {
      simpMessageSendingOperations.convertAndSend(
          "/topic/post/" + postId,
          Message.builder().messageType(MessageType.POST_WAS_TAKEN).build());
    }
  }

  // FOR COLLECTING CASH BY SHIPPER
  public PaymentResponse paid(final String postId) {
    final Payment payment = paymentRepository.findByPostId(postId).orElse(null);
    final Post post = postRepository.findById(postId).orElse(null);
    if (payment != null && post != null) {

      postHistoryRepository.save(
          PostHistory.builder()
              .status(PostStatus.COLLECTED_CASH)
              .post(post)
              .statusChangeDate(LocalDateTime.now())
              .build());

      payment.setPaidAt(LocalDateTime.now());
      return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }
    return null;
  }

  // NOTE: for api test only
  public void pushShipperLocationToUser(final String postId) {

  }
}
