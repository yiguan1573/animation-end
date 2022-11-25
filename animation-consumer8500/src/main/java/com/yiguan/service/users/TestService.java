package com.yiguan.service.users;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: lw
 * @CreateTime: 2022-10-05  22:20
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@FeignClient(value = "animation-users")
public interface TestService {

    @GetMapping(value = "/getConfig")
    String getConfig(@RequestParam(value = "id") String id,@RequestParam(value = "name") String name);
}
