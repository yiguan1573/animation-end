package com.yiguan.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.common.Constant;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  15:54
 * @Description: BlockHandler
 * @Version: 1.0
 */
public class BlockHandler {

    public static DataResult globalBlockHandler(String id, String name, BlockException exception){
        return DataResult.createByError(Constant.FORBIDDEN,exception.toString());
    }
}
