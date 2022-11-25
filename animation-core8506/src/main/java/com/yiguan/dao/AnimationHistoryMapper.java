package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationHistory;
import com.yiguan.bean.vo.AnimationHistoryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationHistoryMapper extends BaseMapper<AnimationHistory> {

    @Select({
            "<script>",
            "select t1.*,t2.mini_image_url,t2.animation_name,t2.animation_total from (select * from animation_history where deleted = 0 and user_id = #{userId}) t1 inner join animation_info t2 on t1.animation_id = t2.id ",
            "<if test='keyword != null'>",
            "where t2.animation_name like '%${keyword}%' ",
            "</if>",
            " order by t1.update_time desc ",
            "limit ${(pageNo - 1) * pageSize} , #{pageSize}",
            "</script>"
    })
    List<AnimationHistoryVo> getAnimationHistoryList(@Param("pageNo") Integer pageNo,@Param("pageSize") Integer pageSize,@Param("keyword") String keyword,@Param("userId") Integer userId);

    @Select({
            "<script>",
            "select count(*) from (select * from animation_history where deleted = 0 and user_id = #{userId}) t1 inner join animation_info t2 on t1.animation_id = t2.id ",
            "<if test='keyword != null'>",
            "where t2.animation_name like '%${keyword}%' ",
            "</if>",
            "</script>"
    })
    Integer getAnimationHistoryTotal(@Param("keyword") String keyword,@Param("userId") Integer userId);

}
