package com.foodmate.backend.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.FileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class S3Deleter {

    private final AmazonS3 amazonS3;
    private final String bucketName;

    public S3Deleter(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }

    public void deleteObject(String objectKey) throws IOException {
        try {
            // 객체 삭제를 요청합니다.
            amazonS3.deleteObject(bucketName, objectKey);
        } catch (AmazonServiceException e) {
            // Amazon S3 서비스 오류 처리
            //e.printStackTrace();
            throw new FileException(Error.S3_SERVICE_ERROR);
        } catch (SdkClientException e) {
            // Amazon S3 클라이언트 오류 처리
            //e.printStackTrace();
            throw new FileException(Error.S3_CLIENT_ERROR);

        }
    }
}