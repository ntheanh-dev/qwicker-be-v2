package com.nta.controller;

import com.nta.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class Test {
    private final PostService postService;

    @GetMapping("/push-delivery-request/{id}")
    public void pushDeliveryRequest(@PathVariable(value = "id") String postId) {
        postService.pushDeliveryRequestToShipper(postId);
    }
//    @GetMapping("/get-delivery-request/{id}")
//    public void getDeliveryRequest(@RequestParam String postId) {
//        postService.handleFoundShipper(postId);
//    }

    @PostMapping("/push-location/post/{id}")
    @PreAuthorize("hasRole('SHIPPER')")
    public void pushLocationToUser(@PathVariable(value = "id") String postId) {
        postService.pushShipperLocationToUser(postId);
    }

}
