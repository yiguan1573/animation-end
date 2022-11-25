package com.yiguan.handler;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiguan.bean.SecurityUser;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.common.Constant;
import com.yiguan.dao.AuthorizationInfoMapper;
import com.yiguan.dao.RoleInfoMapper;
import com.yiguan.dao.UserInfoMapper;
import com.yiguan.util.Oauth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: lw
 * @CreateTime: 2022-10-09  22:59
 * @Description: TODO:鉴权
 * @Version: 1.0
 */
@Component
public class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${token.refresh-expiration}")
    private Integer refreshExpiration;
    @Value("${token.access-expiration}")
    private Integer accessExpiration;
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    @Autowired
    private AuthorizationInfoMapper authorizationInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${server.port}")
    private String port;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private Oauth2Utils oauth2Utils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        String username = principal.getUsername();
        //根据角色查出可以访问的路径
//        List<String> roles = principal.getAuthorities().stream().map(m -> m.getAuthority()).collect(Collectors.toList());
//        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
//        roleInfoQueryWrapper.in("role_name",roles);
//        List<RoleInfo> roleInfos = roleInfoMapper.selectList(roleInfoQueryWrapper);
//        if(CollectionUtils.isEmpty(roleInfos)){
//            throw new RuntimeException("找不到该角色");
//        }
//        QueryWrapper<AuthorizationInfo> authorizationInfoQueryWrapper = new QueryWrapper<>();
//        authorizationInfoQueryWrapper.in("id",roleInfos.stream().map(m -> m.getAuthorizationList()).flatMap(e -> Arrays.stream(e.split(","))).collect(Collectors.toList()));
//        List<AuthorizationInfo> authorizationInfos = authorizationInfoMapper.selectList(authorizationInfoQueryWrapper);
//        if(CollectionUtils.isEmpty(authorizationInfos)){
//            throw new RuntimeException("找不到该权限");
//        }

        //请求oauth2授权服务器获取token
        QueryWrapper<UserInfo> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name",username);
        List<UserInfo> userInfos = userInfoMapper.selectList(userQueryWrapper);

        DataResult postForObject = oauth2Utils.getToken(username,userInfos.get(0).getPassword(), Constant.TOKEN_URL);

        //存入redis，集成oauth2后/oauth/check_token会返回用户的角色权限，不需要自己再存入了
//        redisTemplate.opsForValue().set("auth2_"+username,authorizationInfos.stream().map(AuthorizationInfo::getAuthorizationUrl).collect(Collectors.toList()),tokenExpiration, TimeUnit.DAYS);
        //存入refreshToken
        LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)postForObject.getData();
        data.put("id",userInfos.get(0).getId());
//        Map<String, Object> objectMap = BeanUtils.beanToMap(postForObject.getData());
        redisTemplate.opsForValue().set("refresh_token_"+userInfos.get(0).getId(),data.get("refresh_token"),refreshExpiration, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("access_token_"+userInfos.get(0).getId(),data.get("access_token"),accessExpiration,TimeUnit.DAYS);
        response.setContentType("text/json;charset=utf-8");
        //塞到HttpServletResponse中返回给前台
        response.getWriter().write(JSON.toJSONString(postForObject));
    }
}
