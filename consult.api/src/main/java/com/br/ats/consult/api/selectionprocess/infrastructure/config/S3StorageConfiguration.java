package com.br.ats.consult.api.selectionprocess.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "app.uploads.storage", havingValue = "s3")
public class S3StorageConfiguration {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(@Value("${app.uploads.s3.region:auto}") String region,
                             @Value("${app.uploads.s3.endpoint:}") String endpoint,
                             @Value("${app.uploads.s3.access-key:}") String accessKey,
                             @Value("${app.uploads.s3.secret-key:}") String secretKey,
                             @Value("${app.uploads.s3.force-path-style:false}") boolean forcePathStyle) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(StringUtils.hasText(region) ? region : "auto"))
                .forcePathStyle(forcePathStyle);

        if (StringUtils.hasText(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }

        boolean hasAccessKey = StringUtils.hasText(accessKey);
        boolean hasSecretKey = StringUtils.hasText(secretKey);
        if (hasAccessKey != hasSecretKey) {
            throw new IllegalStateException(
                    "app.uploads.s3.access-key e app.uploads.s3.secret-key devem ser definidos em conjunto.");
        }
        if (hasAccessKey) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
