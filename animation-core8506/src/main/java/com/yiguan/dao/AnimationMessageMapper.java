package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationMessage;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AnimationMessageMapper extends BaseMapper<AnimationMessage> {
    /**
     * 批量插入（mysql）
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<AnimationMessage> entityList);
}
