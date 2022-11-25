package com.yiguan.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author: lw
 * @CreateTime: 2022-11-16  17:10
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@Component
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;
    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 注入minio 客户端
     * @return
     */
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(MyMinioClient.class)
    public MyMinioClient minioClient(){
        MinioClient build = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        return new MyMinioClient(build);
    }
}
