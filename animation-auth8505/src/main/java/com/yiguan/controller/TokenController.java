package com.yiguan.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiguan.bean.SecurityUser;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.common.Constant;
import com.yiguan.config.MyUserDetailsService;
import com.yiguan.dao.UserInfoMapper;
import com.yiguan.util.Oauth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.redis.JdkSerializationStrategy;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lw
 * @CreateTime: 2022-10-20  19:13
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/token")
public class TokenController {
    @Autowired
    private Oauth2Utils oauth2Utils;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    MyUserDetailsService userDetailsService;
    @Value("${token.access-expiration}")
    private Integer accessExpiration;

    @PostMapping("/refreshToken")
    public DataResult refreshToken(String username){
//        RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
//        tokenStore.storeAccessToken();

        //根据用户名获取refresh_token
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("user_name",username);
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if(CollectionUtil.isEmpty(userInfos)){
            return DataResult.createByError("该用户名不存在");
        }
        String key = "refresh_token_"+userInfos.get(0).getId();
        //获取refresh_token
        String refreshToken = redisTemplate.opsForValue().get(key)+ "";
        if("null".equals(refreshToken)){
            return DataResult.createByError("refresh_token不存在");
        }
        DataResult dataResult = oauth2Utils.refreshToken(refreshToken, Constant.TOKEN_URL);
        if(HttpStatus.HTTP_OK == dataResult.getStatus()){
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) dataResult.getData();
            if(data.containsKey("access_token")){
                //设置新的token
                redisTemplate.opsForValue().set("access_token_"+userInfos.get(0).getId(),data.get("access_token"),accessExpiration, TimeUnit.DAYS);
            }
        }
        return dataResult;
    }

    @PostMapping("/refreshCash")
    public DataResult refreshCash(Integer id){
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("id",id);
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if(CollectionUtils.isEmpty(userInfos)){
            return DataResult.createByError("用户不存在");
        }

        // 读取token
        Object object = redisTemplate.opsForValue().get("access_token_" + userInfos.get(0).getId());
        if(object==null){
            return DataResult.createByError("token缓存不存在");
        }
        RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(object.toString());
        // 读取认证信息
        OAuth2Authentication authentication = tokenStore.readAuthentication(accessToken);
        Object principal = authentication.getPrincipal();
        UserDetails user = userDetailsService.loadUserByUsername(userInfos.get(0).getUserName());
        if (principal instanceof SecurityUser) {
            ((SecurityUser)principal).setUsername(user.getUsername());
            ((SecurityUser)principal).setAuthorities((Set<GrantedAuthority>) user.getAuthorities());
        }
        //构建新的Authentication
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(principal, authentication.getCredentials(), user.getAuthorities());
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(authentication.getOAuth2Request(), usernamePasswordAuthenticationToken);
        // 更新缓存信息
        tokenStore.storeAccessToken(accessToken, oAuth2Authentication);
        return DataResult.createBySuccess();
    }
}
