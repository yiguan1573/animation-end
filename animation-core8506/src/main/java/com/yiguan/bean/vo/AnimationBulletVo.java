package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AnimationBullet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Author: lw
 * @CreateTime: 2022-11-24  20:39
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationBulletVo extends AnimationBullet {
    private Boolean isSelf = false;
    private Map<String,String> style;
    private Boolean isJs = false;
    private String direction;
    private Float time;
}
