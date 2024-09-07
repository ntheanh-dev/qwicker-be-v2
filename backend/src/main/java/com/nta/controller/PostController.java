package com.nta.controller;

import com.nta.dto.request.UpdatePostStatusRequest;
import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.NumShipperJoinedResponse;
import com.nta.entity.Post;
import com.nta.service.PostService;
import com.nta.service.ShipperPostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    PostService postService;
    ShipperPostService shipperPostService;
    @PostMapping
    ApiResponse<Post> createPost(@RequestBody PostCreationRequest request) {
        var response = postService.createPost(request);
        return ApiResponse.<Post>builder()
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<Post> getPost(
            @RequestParam(value = "status", required = false) String status,
            @PathVariable String id
    ) {
        Post response;
        if (status != null) {
            response = postService.getPostByStatus(status, id);
        } else {
            response = postService.findById(id);
        }
        return ApiResponse.<Post>builder().result(response).build();
    }

    @PostMapping("/{id}/update")
    ApiResponse<Post> updatePostStatus(
            @PathVariable String id,
            @RequestBody UpdatePostStatusRequest request
    ) {
        Post response = postService.updatePostStatus(request.getStatus(), id, request.getPhoto(), request.getDescription());
        return ApiResponse.<Post>builder().result(response).build();
    }

    @GetMapping("/{id}/num-shipper-joined")
    ApiResponse<NumShipperJoinedResponse> getNumShipperJoined(
            @PathVariable String id
    ) {
    final var response =
        NumShipperJoinedResponse.builder().num(shipperPostService.countByPostId(id)).build();
        return ApiResponse.<NumShipperJoinedResponse>builder().result(response).build();
    }

    @GetMapping
    ApiResponse<List<Post>> getAllPosts(@RequestParam(value = "status", required = false) String statusList) {
        List<Post> response = null;
        try {
            response = postService.getPostsByLatestStatus(statusList);
        } catch (IllegalArgumentException e) {
            response = List.of();
        }

        return ApiResponse.<List<Post>>builder().result(response).build();
    }

    @PostMapping("/{id}")
    ApiResponse<Void> joinPost(@PathVariable String id) {
        postService.joinPost(id);
        return ApiResponse.<Void>builder().result(null).build();
    }

}
