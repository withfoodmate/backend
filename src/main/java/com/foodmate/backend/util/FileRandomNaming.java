package com.foodmate.backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class FileRandomNaming {

    public static String fileRandomNaming(MultipartFile file){
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return  UUID.randomUUID().toString() + fileExtension;
    }
}