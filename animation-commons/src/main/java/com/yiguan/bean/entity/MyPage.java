package com.yiguan.bean.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-13  18:15
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPage {
    private Integer pageNo;
    private Integer pageSize;
    private Integer total;
}
