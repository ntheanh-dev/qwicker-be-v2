package com.nta.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class EmailService {
    JavaMailSender sender;
    Configuration freeMakerConfig;

    //model = {
    //    receiver: toEmail
    //    otp
    //    name
    //    }
    public void sendOPTToNewEmail(String username,String toEmail) throws MessagingException, IOException, TemplateException {
        String subject = "Mã OTP Xác Thực Cho Tài Khoản Của Bạn";
        String templateName = "verify-otp.ftl";

        Map<String,Object> model = new HashMap<>();
        String otp = String.valueOf((int)(Math.random() * 9000) + 1000);
        model.put("otp",otp);
        model.put("username", username);
        sentEmail(subject,toEmail,templateName,model);
    }

    public void sentEmail(String subject,String toEmail,String templateName,Map<String,Object> model) throws MessagingException, IOException, TemplateException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject(subject);
        helper.setTo(toEmail);

        //set the email content as html
        Template template = freeMakerConfig.getTemplate(templateName);
        String htmlText = FreeMarkerTemplateUtils.processTemplateIntoString(template,model);
        helper.setText(htmlText,true);

        sender.send(mimeMessage);
    }
}
