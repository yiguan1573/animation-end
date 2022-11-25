package com.yiguan.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  17:58
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@MapperScan({"com.yiguan.dao"})
public class MyBatisConfig {
}
