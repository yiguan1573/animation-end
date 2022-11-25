package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationInfo;
import com.yiguan.bean.vo.AnimationInfoVo;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationInfoMapper extends BaseMapper<AnimationInfo> {

    //随机查出几条记录，该方法效率很低，权宜之计
    @Select("select * from animation_info where deleted = 0 order by rand() limit #{limit}")
    List<AnimationInfoVo> getRandomAnimationInfo(Integer limit);
}
