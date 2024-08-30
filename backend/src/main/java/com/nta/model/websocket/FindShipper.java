package com.nta.model.websocket;

import com.nta.entity.Post;
import lombok.Data;

@Data
public class FindShipper {
    Post post;
    int km;
}
