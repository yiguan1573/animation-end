package com.yiguan.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.common.Constant;
import com.yiguan.dao.UserInfoMapper;
import com.yiguan.util.Oauth2Utils;
import com.yiguan.util.ResponseUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * @Author: lw
 * @CreateTime: 2022-10-10  20:40
 * @Description: TODO:退出时需要在请求头传入username
 * @Version: 1.0
 */
@Component
public class MyLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Value("${server.port}")
    private String port;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private Oauth2Utils oauth2Utils;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws UnknownHostException {
        String token = httpServletRequest.getHeader("authorization");
        if (token != null) {
            token = token.substring("Bearer ".length());
            String username;
            if(StringUtils.isNotEmpty(httpServletRequest.getHeader("username"))) {
                username = httpServletRequest.getHeader("username");
            }else {
                //根据token获取用户名
                Map<String, Object> checkToken = oauth2Utils.checkToken(token, Constant.CHECK_TOKEN_URL);
                username = checkToken.get("user_name")+"";
            }
//            //清空当前用户缓存中的权限数据
//            redisTemplate.delete("auth2_"+username);
            //清除token
            RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
            redisTokenStore.removeAccessToken(token);
            //根据用户名查找id
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("user_name",username);
            List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
            if(CollectionUtil.isNotEmpty(userInfos)){
                String refreshTokenKey = "refresh_token_"+userInfos.get(0).getId();
                String accessTokenKey = "access_token_"+userInfos.get(0).getId();
                //获取refresh_token
                String refreshToken = redisTemplate.opsForValue().get(refreshTokenKey)+ "";
                redisTemplate.delete(refreshTokenKey);
                redisTemplate.delete(accessTokenKey);
                //删除refresh_token
                redisTokenStore.removeRefreshToken(refreshToken);
            }
            ResponseUtil.out(httpServletResponse, DataResult.createBySuccess("退出成功"));
            return;
        }
        ResponseUtil.out(httpServletResponse, DataResult.createByError("退出失败，token为空"));
    }
}
