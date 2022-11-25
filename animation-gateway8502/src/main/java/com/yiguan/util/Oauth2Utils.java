package com.yiguan.util;

import com.yiguan.bean.entity.DataResult;
import com.yiguan.common.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author: lw
 * @CreateTime: 2022-10-16  18:03
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class Oauth2Utils {
    @Autowired
    private RestTemplate restTemplate;

    public DataResult getToken(String username,String password,String url){
        //组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //必须设置 basicAuth为对应的 clienId、 clientSecret
        headers.setBasicAuth("animation", "123456");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("token", token);
        params.add("username",username);
        params.add("password",password);
        params.add("grant_type","password");
        params.add("scope","all");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(url, entity, DataResult.class);
    }

    public Map<String,Object> checkToken(String token,String url){
        //组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        //必须设置 basicAuth为对应的 clienId、 clientSecret
//        headers.setBasicAuth("animation", "123456");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", token);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(url, entity, Map.class);
    }

    public DataResult refreshToken(String refreshToken,String url){
        //组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //必须设置 basicAuth为对应的 clienId、 clientSecret
        headers.setBasicAuth("animation", "123456");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("refresh_token", refreshToken);
        params.add("grant_type","refresh_token");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(url, entity, DataResult.class);
    }

    public DataResult refreshTokenByusername(String username){
        //组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //必须设置 basicAuth为对应的 clienId、 clientSecret
//        headers.setBasicAuth("animation", "123456");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(Constant.REFRESH_TOKEN_URL, entity, DataResult.class);
    }

    public DataResult logout(String token,String username){
        //组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);
        headers.set("username",username);
        //必须设置 basicAuth为对应的 clienId、 clientSecret
//        headers.setBasicAuth("animation", "123456");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        return restTemplate.postForObject(Constant.LOGOUT_URL, entity, DataResult.class);
    }
}
