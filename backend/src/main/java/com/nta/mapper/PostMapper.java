package com.nta.mapper;

import com.nta.dto.response.PostResponse;
import com.nta.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
