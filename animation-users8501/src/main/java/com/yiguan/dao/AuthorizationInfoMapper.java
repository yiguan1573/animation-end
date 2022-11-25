package com.yiguan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yiguan.bean.entity.AuthorizationInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AuthorizationInfoMapper extends BaseMapper<AuthorizationInfo> {
}
