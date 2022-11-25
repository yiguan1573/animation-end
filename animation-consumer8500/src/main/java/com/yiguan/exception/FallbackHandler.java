package com.yiguan.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.common.Constant;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  15:53
 * @Description: fallback 函数可以针对所有类型的异常（除了exceptionsToIgnore里面排除掉的异常类型）进行处理
 * @Version: 1.0
 */
public class FallbackHandler {
    public static DataResult globalFallbackHandler(String id,String name, Throwable  exception){
        return DataResult.createByError(Constant.FORBIDDEN,exception.getMessage());
    }
}
