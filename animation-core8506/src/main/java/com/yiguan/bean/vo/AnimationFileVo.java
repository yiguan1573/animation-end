package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AnimationFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-18  16:02
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationFileVo extends AnimationFile {
    private String animationName;
    private Integer animationTotal;
}
