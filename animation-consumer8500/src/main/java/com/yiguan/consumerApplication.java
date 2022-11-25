package com.yiguan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: lw
 * @CreateTime: 2022-10-05  20:39
 * @Description: TODO
 * @Version: 1.0
 */
//取消数据源的自动创建，而是使用自己定义的
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
public class consumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(consumerApplication.class,args);
    }
}
