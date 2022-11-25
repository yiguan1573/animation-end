package com.yiguan.util;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiguan.bean.entity.DataResult;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.CharSetUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: lw
 * @CreateTime: 2022-10-08  23:09
 * @Description: TODO
 * @Version: 1.0
 */
public class ResponseUtil {
    public static void out(HttpServletResponse response, DataResult dataResult){
        response.setStatus(HttpStatus.HTTP_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response.getWriter().write(objectMapper.writeValueAsString(dataResult));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
