package com.project.ssrapi.configs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSS3Configuration {
    @Bean
    public AmazonS3 amazonS3(
            AWSS3Properties awss3Properties
    ){

        if(awss3Properties.getAccessKey() != null && awss3Properties.getSecretKey() != null){
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(awss3Properties.getAccessKey(), awss3Properties.getSecretKey());
            return AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.fromName(awss3Properties.getRegion()))
                    .build();
        }else{
            return AmazonS3ClientBuilder.standard().build();
        }
    }
}
