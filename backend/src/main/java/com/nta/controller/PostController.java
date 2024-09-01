package com.nta.controller;

import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.entity.Post;
import com.nta.service.PostService;
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

    @PostMapping
    ApiResponse<Post> createPost(@RequestBody PostCreationRequest request) {
        var response = postService.createPost(request);
        return ApiResponse.<Post>builder()
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<Post> getPostById(@PathVariable String id) {
        var response = postService.findById(id);
        return ApiResponse.<Post>builder().result(response).build();
    }

    @GetMapping
    ApiResponse<List<Post>> getAllPosts(@RequestParam(value = "status",required = false) String statusList) {
        List<Post> response = null;
        try {
            response = postService.getPosts(statusList);
        } catch (IllegalArgumentException e) {
            response = List.of();
        }

        return ApiResponse.<List<Post>>builder().result(response).build();
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('SHIPPER')")
    ApiResponse<Void> joinPost(@PathVariable String id) {
        postService.joinPost(id);
        return ApiResponse.<Void>builder().result(null).build();
    }

}
