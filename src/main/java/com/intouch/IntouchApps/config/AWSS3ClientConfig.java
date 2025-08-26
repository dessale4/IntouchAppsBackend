package com.intouch.IntouchApps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RefreshScope
public class AWSS3ClientConfig {
    @Value("${application.aws.s3.access_key}")
    private String awsS3AccessKey;
    @Value("${application.aws.s3.secret_key}")
    private String awsS3SecretKey;
    @Value("${application.aws.s3.bucket_name}")
    private String awsS3BucketName;
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(awsS3AccessKey, awsS3SecretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        return S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
