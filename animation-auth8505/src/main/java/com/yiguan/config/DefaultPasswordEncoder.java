package com.yiguan.config;

import cn.hutool.crypto.SecureUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @Author: lw
 * @CreateTime: 2022-10-11  20:22
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class DefaultPasswordEncoder implements PasswordEncoder {
    public DefaultPasswordEncoder() {
        this(-1);
    }
    /**
     * @param strength

     * the log rounds to use, between 4 and 31
     */

    public DefaultPasswordEncoder(int strength) {
    }
    public String encode(CharSequence rawPassword) {
        return SecureUtil.md5(rawPassword.toString());
    }
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(SecureUtil.md5(rawPassword.toString()));
    }
}
