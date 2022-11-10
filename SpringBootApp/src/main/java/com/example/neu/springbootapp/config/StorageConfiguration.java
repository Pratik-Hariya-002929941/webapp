package com.example.neu.springbootapp.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.example.neu.springbootapp.controller.UsersController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class StorageConfiguration {

    private static final Logger logger =Logger.getLogger(UsersController.class.getName());

    private String accessKey = System.getenv("AWS_ACCESS_KEY_ID");

    private String accessSecreteKey = System.getenv("AWS_SECRET_KEY_ID");

    private String awsRegion = System.getenv("AWS_REGION");

    @Bean
    public AmazonS3 s3Client() {
        logger.log(Level.INFO, accessKey);
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecreteKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(awsRegion).build();
    }
}
