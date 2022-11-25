package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @Author: lw
 * @CreateTime: 2022-11-22  14:03
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimationHistory {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    @NotNull
    private Integer animationId;
    @NotNull
    private Integer userId;
    @NotNull
    private Integer episodeNo;
    @NotNull
    private Integer specificTime;
    private Integer deleted;
    private Long updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimationHistory that = (AnimationHistory) o;
        return Objects.equals(animationId, that.animationId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationId, userId);
    }
}
