package com.nta.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreationRequest {
    private String username;
    //@Size(message=Errorcode.PASSWORD_INVALID.getMessage())
    // Không thể truyền như vậy do bắt buộc phải truyền vào một constanins
    // => Truyền vào một ENUM NAME và trong handler lấy ra Errorcode tướng ứng
    @Size(min = 8 , message = "PASSWORD_INVALID")
    private String password;
    private String firstName;
    private String lastName;
    private String email;
}
