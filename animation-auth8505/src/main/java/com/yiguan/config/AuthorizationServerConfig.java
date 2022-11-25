package com.yiguan.config;

import com.yiguan.filter.CustomClientCredentialsTokenEndpointFilter;
import com.yiguan.handler.CustomAuthenticationEntryPoint;
import com.yiguan.handler.OauthExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;

/**
 * @Author: lw
 * @CreateTime: 2022-10-15  16:19
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter  {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    DefaultPasswordEncoder defaultPasswordEncoder;
    @Autowired
    DataSource dataSource;
    @Autowired
    OauthExceptionHandler oauthExceptionHandler;
    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Autowired
    MyUserDetailsService userDetailsService;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.jdbc(dataSource).
        //配置两个客户端,一个用于password认证一个用于client认证
        clients.jdbc(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(new RedisTokenStore(redisConnectionFactory))
                .authenticationManager(authenticationManager)
                //设置userDetailsService刷新token时候会用到
                .userDetailsService(userDetailsService);
        endpoints.exceptionTranslator(oauthExceptionHandler);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        CustomClientCredentialsTokenEndpointFilter endpointFilter = new CustomClientCredentialsTokenEndpointFilter(oauthServer);
        endpointFilter.afterPropertiesSet();//初始化的时候执行
        endpointFilter.setAuthenticationEntryPoint(customAuthenticationEntryPoint);//格式化客户端异常的响应格式
        oauthServer.addTokenEndpointAuthenticationFilter(endpointFilter);//添加一个客户端认证之前的过滤器
        // 对获取Token的请求不再拦截
        oauthServer
                .tokenKeyAccess("permitAll()")
                // 验证获取Token的验证信息
                .checkTokenAccess("permitAll()");
                //这个如果配置支持allowFormAuthenticationForClients的，且对/oauth/token请求的参数中有client_id和client-secret的会走ClientCredentialsTokenEndpointFilter来保护
                //如果没有支持allowFormAuthenticationForClients或者有支持但对/oauth/token请求的参数中没有client_id和client_secret的，走basic认证保护
//                .allowFormAuthenticationForClients();
        /*
         * allowFormAuthenticationForClients 的作用:
         * 允许表单认证(申请令牌), 而不仅仅是Basic Auth方式提交, 且url中有client_id和client_secret的会走 ClientCredentialsTokenEndpointFilter 来保护，
         * 也就是在 BasicAuthenticationFilter 之前添加 ClientCredentialsTokenEndpointFilter，使用 ClientDetailsService 来进行 client 端登录的验证。
         * 但是，在使用自定义的 CustomClientCredentialsTokenEndpointFilter 时,
         * 会导致 oauth2 仍然使用 allowFormAuthenticationForClients 中默认的 ClientCredentialsTokenEndpointFilter 进行过滤，致使我们的自定义 CustomClientCredentialsTokenEndpointFilter 不生效。
         * 因此在使用 CustomClientCredentialsTokenEndpointFilter 时，不再需要开启 allowFormAuthenticationForClients() 功能。
         */
    }

}
