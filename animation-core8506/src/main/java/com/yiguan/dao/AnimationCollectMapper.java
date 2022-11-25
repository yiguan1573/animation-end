package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationCollect;
import com.yiguan.bean.vo.AnimationInfoVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationCollectMapper extends BaseMapper<AnimationCollect> {

    @Select({
            "<script>",
                "select t2.* from (select * from animation_collect where deleted = 0 and user_id = #{userId}) t1 inner join animation_info t2 on t1.animation_id = t2.id ",
                "<if test='keyword != null'>",
                    "where t2.animation_name like '%${keyword}%' ",
                "</if>",
                "limit ${(pageNo - 1) * pageSize} , #{pageSize}",
            "</script>"
    })
    List<AnimationInfoVo> getCollectList(@Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize, @Param("keyword") String keyword, @Param("userId") Integer userId);

    @Select({
            "<script>",
                "select count(*) from (select * from animation_collect where deleted = 0 and user_id = #{userId}) t1 inner join animation_info t2 on t1.animation_id = t2.id ",
                "<if test='keyword != null'>",
                    "where t2.animation_name like '%${keyword}%' ",
                "</if>",
            "</script>"
    })
    Integer getCollectListTotal(@Param("keyword") String keyword, @Param("userId") Integer userId);
}
