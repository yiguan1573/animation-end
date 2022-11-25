package com.yiguan.service;

import com.yiguan.bean.entity.DataResult;

public interface CommonService {

    DataResult commonAnimationList(Integer pageNo, Integer pageSize, String keyword,  Integer type);

    DataResult getRecommend(Integer limit,Integer userId);

    DataResult getDetail(Integer animationId,Integer userId);

    DataResult getBulletList(Integer animationId,Integer episodeNo,Integer userId);
}
