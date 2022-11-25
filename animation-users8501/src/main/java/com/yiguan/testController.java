package com.yiguan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: lw
 * @CreateTime: 2022-10-05  19:39
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RefreshScope
@Slf4j
public class testController {
    @Value("${server.port}")
    private String serverPort;
    @Value("${config.info}")
    private String config;

    /**
     * @author: lw
     * @date: 2022/10/5 20:06
     * @param: id
     * @param: name
     * @return: String
     * @description:
     **/
    @GetMapping(value = "/getConfig")
    public String getConfig(String id,String name){
        return String.format("端口号：%s,配置：%s",serverPort,config);
    }
}
