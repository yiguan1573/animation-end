package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationBullet;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AnimationBulletMapper extends BaseMapper<AnimationBullet> {
    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<AnimationBullet> entityList);
}
