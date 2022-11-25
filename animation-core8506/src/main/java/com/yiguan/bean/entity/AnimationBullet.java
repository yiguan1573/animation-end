package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: lw
 * @CreateTime: 2022-11-24  16:49
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationBullet implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private Integer animationId;
    private Integer userId;
    private Integer episodeNo;
    private String content;
    private Integer type;
    private String color = "white";
    private Float sendTime;
}
