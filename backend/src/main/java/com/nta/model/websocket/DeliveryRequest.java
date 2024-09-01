package com.nta.model.websocket;

import com.nta.dto.response.PostResponse;
import com.nta.entity.Post;
import com.nta.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryRequest {
    private PostResponse post;
    private MessageType messageType;
}
