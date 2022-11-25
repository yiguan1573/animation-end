package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AnimationHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-22  14:39
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationHistoryVo  extends AnimationHistory {
    private String time;
    private String miniImageUrl;
    private String animationName;
    private String animationTotal;

}
