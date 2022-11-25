package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author: lw
 * @CreateTime: 2022-11-25  16:55
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationMessage {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String updateTime;
}
