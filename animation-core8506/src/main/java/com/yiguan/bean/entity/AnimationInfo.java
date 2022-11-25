package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: lw
 * @CreateTime: 2022-11-17  14:31
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimationInfo {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    @NotBlank
    private String animationName;
    private String miniImageUrl;
    private String carouselImageUrl;
    @NotNull
    private Integer animationType;
    @NotNull
    private Integer animationTotal;
    private String animationDetail;
    @NotBlank
    private String originPlace;
    private Integer deleted;

}
