package com.yiguan.service;


import com.yiguan.bean.entity.AnimationHistory;
import com.yiguan.bean.entity.DataResult;

public interface CoreService {

    DataResult collect(Integer status, Integer userId, Integer animationId);

    DataResult getCollectList(Integer pageNo, Integer pageSize, String keyword, Integer userId);

    DataResult recordHistory(AnimationHistory animationHistory);

    DataResult getHistoryList(Integer pageNo, Integer pageSize, String keyword, Integer userId);

    DataResult deleteHistory(Integer id,Integer userId);

    DataResult getGlobalMessage(Integer pageNo, Integer pageSize, String keyword);

}
