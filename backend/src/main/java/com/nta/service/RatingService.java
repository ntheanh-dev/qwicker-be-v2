package com.nta.service;

import com.nta.dto.request.RatingCreationRequest;
import com.nta.entity.Post;
import com.nta.entity.Rating;
import com.nta.entity.Shipper;
import com.nta.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final PostService postService;
    private final ShipperService shipperService;
    public void createRating(final RatingCreationRequest request) {
        final Post post = postService.findById(request.getPostId());
        final Shipper shipper = shipperService.findById(request.getShipperId());
        final Rating rating = Rating.builder()
                .post(post)
                .shipper(shipper)
                .rating(request.getRating())
                .feedback(request.getFeedback())
                .build();
        ratingRepository.save(rating);
    }
}
