package com.yiguan.config;

import com.yiguan.handler.MyAuthenticationFailHandler;
import com.yiguan.handler.MyAuthenticationSuccessHandler;
import com.yiguan.handler.MyLogoutSuccessHandler;
import com.yiguan.handler.UnauthorizedEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: lw
 * @CreateTime: 2022-10-09  22:55
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    MyUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DefaultPasswordEncoder();
    }

    @Autowired
    MyAuthenticationFailHandler failHandler;

    @Autowired
    MyAuthenticationSuccessHandler successHandler;

    @Autowired
    MyLogoutSuccessHandler myLogoutSuccessHandler;

    @Autowired
    UnauthorizedEntryPoint unauthorizedEntryPoint;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setHideUserNotFoundExceptions(false);
        return authenticationProvider;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                //?????????????????????,?????????????????????controller??????
                //???????????????????????????controller?????????????????????????????????????????????
//                .loginPage("/unLogin")
                //????????????????????????,/login??????????????????????????????
                //?????????????????????????????????,???????????????????????????????????????
                .loginProcessingUrl("/login")
                //???????????????????????????
                .successHandler(successHandler)
                //???????????????????????????
                .failureHandler(failHandler)
                .permitAll()
                .and()
                .logout()
                //???????????????????????????
                .logoutSuccessHandler(myLogoutSuccessHandler)
                // ????????????
                .invalidateHttpSession(true)
                // ??????????????????
                .clearAuthentication(true)
                .and().csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedEntryPoint);
        http.authorizeRequests()
                .antMatchers(
                        "/oauth/**",
                        "/login/**",
                        "/logout/**",
                        "/token/**"

                ).permitAll().anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }
}
