package com.nta.controller;

import com.nta.dto.request.RatingCreationRequest;
import com.nta.dto.request.UpdatePostStatusRequest;
import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.*;
import com.nta.entity.Post;
import com.nta.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  PostService postService;
  ShipperPostService shipperPostService;
  RatingService ratingService;
  ShipperService shipperService;

  @PostMapping
  ApiResponse<Post> createPost(@RequestBody PostCreationRequest request) {
    var response = postService.createPost(request);
    return ApiResponse.<Post>builder().result(response).build();
  }

  @GetMapping("/{id}")
  ApiResponse<Post> getPost(
      @RequestParam(value = "status", required = false) String status, @PathVariable String id) {
    Post response;
    if (status != null) {
      response = postService.getPostByStatus(status, id);
    } else {
      response = postService.findById(id);
    }
    return ApiResponse.<Post>builder().result(response).build();
  }

  @PostMapping("/{id}/update")
  @PreAuthorize("hasRole('SHIPPER')")
  ApiResponse<Post> updatePostStatus(
      @PathVariable String id, @RequestBody UpdatePostStatusRequest request) {
    Post response =
        postService.updatePostStatus(
            request.getStatus(), id, request.getPhoto(), request.getDescription());
    return ApiResponse.<Post>builder().result(response).build();
  }

  @PostMapping("/{id}/rating")
  ApiResponse<RatingResponse> postRating(
      @PathVariable String id, @RequestBody RatingCreationRequest request) {
    return ApiResponse.<RatingResponse>builder()
        .result(ratingService.createRating(request, id))
        .build();
  }

  @GetMapping("/{id}/rating")
  ApiResponse<RatingResponse> getRating(@PathVariable String id) {
    return ApiResponse.<RatingResponse>builder().result(ratingService.getRating(id)).build();
  }

  @GetMapping("/{id}/winner")
  ApiResponse<ShipperResponse> getWinner(@PathVariable String id) {
    return ApiResponse.<ShipperResponse>builder()
        .result(shipperPostService.findWinnerByPostId(id))
        .build();
  }

  @GetMapping("/{id}/num-shipper-joined")
  ApiResponse<NumShipperJoinedResponse> getNumShipperJoined(@PathVariable String id) {
    final var response =
        NumShipperJoinedResponse.builder().num(shipperPostService.countByPostId(id)).build();
    return ApiResponse.<NumShipperJoinedResponse>builder().result(response).build();
  }

  @GetMapping
  ApiResponse<List<Post>> getAllPosts(
      @RequestParam(value = "status", required = false) String statusList) {
    List<Post> response;
    try {
      response = postService.getPostsByStatusList(statusList);
    } catch (IllegalArgumentException e) {
      response = List.of();
    }

    return ApiResponse.<List<Post>>builder().result(response).build();
  }

  @PostMapping("/{id}")
  ApiResponse<Void> joinPost(@PathVariable String id) {
    postService.joinPost(id, shipperService.getCurrentShipper().getId());
    return ApiResponse.<Void>builder().result(null).build();
  }

  @PreAuthorize("hasRole('SHIPPER')")
  @PostMapping("/{id}/collect-cash")
  ApiResponse<PaymentResponse> collectCash(@PathVariable String id) {
    return ApiResponse.<PaymentResponse>builder().result(postService.paid(id)).build();
  }

  @PostMapping("/{id}/select-shipper")
  ApiResponse<Void> selectShipper(@PathVariable String id) {
    postService.selectShipperToShip(postService.findById(id));
    return ApiResponse.<Void>builder().result(null).build();
  }
}
