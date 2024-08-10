package com.nta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Getter
@Setter
@Builder
public class UserCreationRequest {
    String username;
    //@Size(message=Errorcode.PASSWORD_INVALID.getMessage())
    // Không thể truyền như vậy do bắt buộc phải truyền vào một constanins
    // => Truyền vào một ENUM NAME và trong handler lấy ra Errorcode tướng ứng
    @Size(min = 8 , message = "PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;
    String email;
    byte[] file;
}
