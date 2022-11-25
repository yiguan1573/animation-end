package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AnimationFile;
import com.yiguan.bean.vo.AnimationFileVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationFileMapper extends BaseMapper<AnimationFile> {

    @Select({
            "<script>",
            "select t1.animation_name,t1.animation_total,t2.* from animation_info t1 inner join animation_file t2 on t1.id = t2.animation_id ",
                    "<if test='search != null'>",
                        "where t1.animation_name like '%${search}%' ",
                    "</if>",
                    "limit ${(pageNo - 1) * pageSize} , #{pageSize}",
            "</script>"
    })
    List<AnimationFileVo> getFileList(@Param("pageSize") Integer pageSize,@Param("pageNo") Integer pageNo,@Param("search") String search);

    @Select({
            "<script>",
            "select count(*) from animation_info t1 inner join animation_file t2 on t1.id = t2.animation_id ",
            "<if test='search != null'>",
            "where t1.animation_name like '%${search}%' ",
            "</if>",
            "</script>"
    })
    Integer getTotalFile(@Param("search") String search);
}
