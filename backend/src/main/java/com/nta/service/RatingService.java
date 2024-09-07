package com.nta.service;

import com.nta.dto.request.RatingCreationRequest;
import com.nta.dto.response.RatingResponse;
import com.nta.entity.Post;
import com.nta.entity.Rating;
import com.nta.entity.Shipper;
import com.nta.mapper.RatingMapper;
import com.nta.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RatingService {
  private final RatingMapper ratingMapper;
  private final RatingRepository ratingRepository;
  private final PostService postService;
  private final ShipperService shipperService;
  private final UserService userService;

  public RatingResponse createRating(final RatingCreationRequest request, final String postId) {
    final Post post = postService.findById(postId);
    final Rating rating =
        Rating.builder()
            .post(post)
            .shipper(shipperService.getWinShipperByPostId(postId))
            .user(userService.currentUser())
            .rating(request.getRating())
            .feedback(request.getFeedback())
            .createdAt(LocalDateTime.now())
            .build();
    return ratingMapper.toRatingResponse(ratingRepository.save(rating));
  }

  public RatingResponse getRating(final String postId) {
    final var rating =
        ratingRepository
            .findByPostIdAndShipperId(postId, userService.currentUser().getId())
            .orElse(null);
    return ratingMapper.toRatingResponse(rating);
  }
}
