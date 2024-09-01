package com.nta.model;

import com.nta.enums.MessageType;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Message {
    private MessageType messageType;
    private String content;
}
