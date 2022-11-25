package com.yiguan.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: lw
 * @CreateTime: 2022-10-15  21:22
 * @Description: TODO:自定义无异常情况下请求 /oauth/token 获取 token 的响应格式
 * @Version: 1.0
 */
@Slf4j
@Aspect
@Component
@Lazy(false)
public class CustomOAuthTokenAspect {
    @Around("execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
    public ResponseEntity response(ProceedingJoinPoint point) throws Throwable {
        Object proceed = point.proceed();
        ResponseEntity<OAuth2AccessToken> responseEntity = (ResponseEntity<OAuth2AccessToken>) proceed;
        ObjectMapper objectMapper = new ObjectMapper();
        return ResponseEntity.ok(objectMapper.writeValueAsString(DataResult.createBySuccess(responseEntity.getBody())));
    }
}
