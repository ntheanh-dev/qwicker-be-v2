package com.nta.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyConfig {

    @Autowired
    private Environment env;

    @Bean
    public Cloudinary cloudinary() {
        Cloudinary cloudinary
                = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", env.getProperty("cloudinary.name"),
                "api_key", env.getProperty("cloudinary.key"),
                "api_secret", env.getProperty("cloudinary.secret"),
                "secure", true));
        return cloudinary;
    }

}
