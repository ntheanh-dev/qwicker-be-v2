package com.nta.service;

import com.nta.entity.ShipperPost;
import com.nta.repository.ShipperPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperPostService {
    private final ShipperPostRepository shipperPostRepository;

    public List<ShipperPost> findAllByPostId(String postId) {
        return shipperPostRepository.findAllByPostId(postId);
    }
}
