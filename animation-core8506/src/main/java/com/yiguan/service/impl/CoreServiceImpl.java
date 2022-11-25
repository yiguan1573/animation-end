package com.yiguan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yiguan.bean.entity.*;
import com.yiguan.bean.vo.AnimationHistoryVo;
import com.yiguan.bean.vo.AnimationInfoVo;
import com.yiguan.bean.vo.AnimationMessageVo;
import com.yiguan.controller.WebSocket;
import com.yiguan.dao.AnimationCollectMapper;
import com.yiguan.dao.AnimationHistoryMapper;
import com.yiguan.dao.AnimationInfoMapper;
import com.yiguan.dao.AnimationMessageMapper;
import com.yiguan.service.CoreService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-11-21  17:00
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class CoreServiceImpl implements CoreService {
    @Autowired
    AnimationInfoMapper animationInfoMapper;
    @Autowired
    AnimationCollectMapper animationCollectMapper;
    @Autowired
    AnimationHistoryMapper animationHistoryMapper;
    @Autowired
    AnimationMessageMapper animationMessageMapper;
    @Autowired
    WebSocket webSocket;

    @Override
    public DataResult collect(Integer status, Integer userId, Integer animationId) {
        QueryWrapper<AnimationCollect> animationCollectQueryWrapper = new QueryWrapper<>();
        animationCollectQueryWrapper.eq("user_id", userId).eq("animation_id", animationId);
        animationCollectMapper.delete(animationCollectQueryWrapper);
        if (status == 0) {//取消收藏
            return DataResult.createBySuccess();
        }
        //收藏
        AnimationCollect animationCollect = new AnimationCollect();
        animationCollect.setAnimationId(animationId);
        animationCollect.setUserId(userId);
        animationCollectMapper.insert(animationCollect);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult getCollectList(Integer pageNo, Integer pageSize, String keyword, Integer userId) {
        List<AnimationInfoVo> collectList = animationCollectMapper.getCollectList(pageNo, pageSize, keyword, userId);
        Integer total = animationCollectMapper.getCollectListTotal(keyword, userId);
        MyPage page = new MyPage(pageNo, pageSize, total);
        collectList.forEach(e -> e.setStatus(1));
        return DataResult.createBySuccess(collectList, page);
    }

    @Override
    public DataResult recordHistory(AnimationHistory animationHistory) {
        //删除原来本用户关于本动画的历史记录
        QueryWrapper<AnimationHistory> animationHistoryQueryWrapper = new QueryWrapper<>();
        animationHistoryQueryWrapper.eq("user_id", animationHistory.getUserId()).eq("animation_id", animationHistory.getAnimationId());
        animationHistoryMapper.delete(animationHistoryQueryWrapper);
        //插入新的历史记录
        animationHistory.setUpdateTime(System.currentTimeMillis());
        animationHistory.setDeleted(0);
        animationHistoryMapper.insert(animationHistory);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult getHistoryList(Integer pageNo, Integer pageSize, String keyword, Integer userId) {
        List<AnimationHistoryVo> animationHistoryList = animationHistoryMapper.getAnimationHistoryList(pageNo, pageSize, keyword, userId);
        Integer total = animationHistoryMapper.getAnimationHistoryTotal(keyword, userId);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        animationHistoryList.forEach(e -> e.setTime(simpleDateFormat.format(new Date(e.getUpdateTime()))));
        MyPage page = new MyPage(pageNo, pageSize, total);
        animationHistoryList = animationHistoryList.stream().distinct().collect(Collectors.toList());
        return DataResult.createBySuccess(animationHistoryList, page);
    }

    @Override
    public DataResult deleteHistory(Integer id, Integer userId) {
        if (userId != null) {//删除该用户所有的历史记录
            QueryWrapper<AnimationHistory> animationHistoryQueryWrapper = new QueryWrapper<>();
            animationHistoryQueryWrapper.eq("user_id",userId);
            animationHistoryMapper.delete(animationHistoryQueryWrapper);
            return DataResult.createBySuccess();
        }
        //删除单条历史记录
        animationHistoryMapper.deleteById(id);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult getGlobalMessage(Integer pageNo, Integer pageSize, String keyword) {
        QueryWrapper<AnimationMessage> messageQueryWrapper = new QueryWrapper<>();
        Page<AnimationMessage> messagePage = new Page<>(pageNo, pageSize);
        if(StringUtils.isNotEmpty(keyword)){
            messageQueryWrapper.like("title",keyword).or().like("content",keyword);
        }
        messageQueryWrapper.orderByDesc("update_time");
        Page<AnimationMessage> animationMessagePage = animationMessageMapper.selectPage(messagePage, messageQueryWrapper);
        List<AnimationMessage> records = animationMessagePage.getRecords();
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(animationMessagePage.getTotal()+""));
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(new ArrayList<>(),myPage);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ArrayList<AnimationMessageVo> messageVos = new ArrayList<>();
        records.forEach(e ->{
            AnimationMessageVo messageVo = new AnimationMessageVo();
            BeanUtils.copyProperties(e,messageVo);
            messageVo.setTime(simpleDateFormat.format(new Date(Long.parseLong(e.getUpdateTime()))));
            messageVos.add(messageVo);
        });
        return DataResult.createBySuccess(messageVos,myPage);
    }
}
