package com.yiguan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @Author: lw
 * @CreateTime: 2022-11-13  22:29
 * @Description: TODO 解决跨域
 * @Version: 1.0
 */
@Configuration
public class CorsConfigurationConfig {

    /**
     * 想要实现跨域，springboot提供了跨域filter，CorsWebFilter类
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        // 2、跨域的配置信息
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 4、配置设置
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 配置跨域
        corsConfiguration.addAllowedHeader("*"); //允许哪些头访问
        corsConfiguration.addAllowedMethod("*"); //允许哪些请求方式进行跨域
        corsConfiguration.addAllowedOrigin("*"); // 允许哪个请求来源进行跨域
        corsConfiguration.setAllowCredentials(true); // 是否允许携带cookie进行跨域
        // 3、设置注册配置，需要CorsConfiguration
        source.registerCorsConfiguration("/**",corsConfiguration);
        // 1、需要传入配置类
        return new CorsWebFilter(source);
    }
}