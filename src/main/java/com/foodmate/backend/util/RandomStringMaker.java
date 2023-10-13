package com.foodmate.backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class RandomStringMaker {

    public static String randomStringMaker(){
        return  UUID.randomUUID().toString();
    }
}