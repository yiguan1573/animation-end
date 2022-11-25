package com.yiguan.bean.dto;

import com.yiguan.bean.entity.AnimationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-18  14:14
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarouselDto extends AnimationInfo {
    private Integer index;
}
