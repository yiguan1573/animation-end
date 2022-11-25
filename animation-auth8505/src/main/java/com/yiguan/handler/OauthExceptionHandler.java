package com.yiguan.handler;

import com.yiguan.bean.entity.DataResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

/**
 * @Author: lw
 * @CreateTime: 2022-10-15  20:39
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class OauthExceptionHandler implements WebResponseExceptionTranslator<OAuth2Exception>{

    @Override
    public ResponseEntity translate(Exception e) throws Exception {
        return ResponseEntity.ok(DataResult.createByError(e.getMessage()));
    }
}
