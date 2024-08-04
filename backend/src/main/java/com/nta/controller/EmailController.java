package com.nta.controller;

import com.nta.dto.response.ApiResponse;
import com.nta.service.EmailService;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class EmailController {
    EmailService emailService;
    @PostMapping("/sent-otp")
    public ApiResponse<Void> sendOTPWhenCreateAccount(@RequestParam Map<String,String> request)
            throws MessagingException, TemplateException, IOException
    {
        String username = request.get("username");
        String email = request.get("email");
        emailService.sendOPTToNewEmail(username,email);
        return ApiResponse.<Void>builder()
                .message("OTP has sent")
                .build();
    }
}
