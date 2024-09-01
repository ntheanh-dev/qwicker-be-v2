package com.nta.controller;

import com.nta.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class Test {
    private final PostService postService;

    @GetMapping("/push-delivery-request/{id}")
    public void pushDeliveryRequest(@PathVariable String id) {
        postService.pushDeliveryRequestToShipper(id);
    }
}
