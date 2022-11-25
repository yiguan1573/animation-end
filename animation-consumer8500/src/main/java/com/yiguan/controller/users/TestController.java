package com.yiguan.controller.users;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.common.Constant;
import com.yiguan.exception.BlockHandler;
import com.yiguan.exception.FallbackHandler;
import com.yiguan.service.users.TestService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lw
 * @CreateTime: 2022-10-05  20:50
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@Slf4j
@RequestMapping("/users")
public class TestController {
    @Resource
    private RestTemplate restTemplate;

    @Value("${service-url.users-service}")
    private String serverURL;

    @Resource
    private TestService testService;

    @PostMapping(value = "/getConfig")
    @SentinelResource(value = "getConfig",
            fallbackClass = FallbackHandler.class,
            fallback = "globalFallbackHandler",
            blockHandlerClass = BlockHandler.class,
            blockHandler = "globalBlockHandler")
    //rollbackFor = Exception.class表示对任意异常都进行回滚
    @GlobalTransactional(name = "fsp_tx_group",rollbackFor = Exception.class)
    public DataResult getConfig(String id,String name){
        return DataResult.createBySuccess(testService.getConfig(id,name));
    }

    @PostMapping(value = "/getConfig1")
    public DataResult getConfig1(){
        return DataResult.createBySuccess("请求成功");
    }
}
