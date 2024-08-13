package com.nta.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;

    public Map upload(MultipartFile file)  {
        try{
            Map data = this.cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            return data;
        }catch (IOException io){
            throw new RuntimeException("Image upload fail");
        }
    }

    public Map upload(byte[] b) {
        try{
            Map data = this.cloudinary.uploader().upload(b, ObjectUtils.asMap("resource_type", "auto"));
            return data;
        }catch (IOException io){
            throw new RuntimeException("Image upload fail");
        }
    }

    public String url(byte[] b) {
        try{
            Map data = this.cloudinary.uploader().upload(b, ObjectUtils.asMap("resource_type", "auto"));
            return data.get("secure_url").toString();
        }catch (IOException io){
            throw new RuntimeException("Image upload fail");
        }
    }
}
