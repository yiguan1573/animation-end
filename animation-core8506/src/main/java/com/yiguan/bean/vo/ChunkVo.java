package com.yiguan.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-20  17:45
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChunkVo {
    private Integer partNum;
    private String chunkUrl;
}
