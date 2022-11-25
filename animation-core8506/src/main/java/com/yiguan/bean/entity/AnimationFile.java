package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lw
 * @CreateTime: 2022-11-18  15:41
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationFile {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private Integer animationId;
    private Integer episodeNo;
    private Double fileSize;
    private String fileName;
}
