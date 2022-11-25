package com.yiguan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  00:06
 * @Description: TODO
 * @Version: 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class gatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(gatewayApplication.class,args);
    }
}
