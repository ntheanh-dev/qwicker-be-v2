package com.nta.model;

import com.nta.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Message {
    private MessageType type;
}
