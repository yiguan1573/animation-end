package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AnimationMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-25  18:00
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimationMessageVo extends AnimationMessage {
    private String time;
}
