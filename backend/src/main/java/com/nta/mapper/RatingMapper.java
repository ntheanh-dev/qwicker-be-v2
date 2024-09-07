package com.nta.mapper;

import com.nta.dto.response.RatingResponse;
import com.nta.entity.Rating;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    RatingResponse toRatingResponse(Rating rating);
}
