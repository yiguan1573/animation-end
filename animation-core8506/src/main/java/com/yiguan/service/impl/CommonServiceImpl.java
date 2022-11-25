package com.yiguan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yiguan.bean.entity.*;
import com.yiguan.bean.vo.AnimationBulletVo;
import com.yiguan.bean.vo.AnimationInfoVo;
import com.yiguan.bean.vo.EpisodeFileVo;
import com.yiguan.dao.AnimationBulletMapper;
import com.yiguan.dao.AnimationCollectMapper;
import com.yiguan.dao.AnimationFileMapper;
import com.yiguan.dao.AnimationInfoMapper;
import com.yiguan.service.CommonService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-11-21  14:15
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class CommonServiceImpl implements CommonService {
    @Autowired
    AnimationInfoMapper animationInfoMapper;
    @Autowired
    AnimationCollectMapper animationCollectMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AnimationFileMapper animationFileMapper;
    @Autowired
    AnimationBulletMapper animationBulletMapper;

    @Override
    public DataResult commonAnimationList(Integer pageNo, Integer pageSize, String keyword, Integer type) {

        Page<AnimationInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<AnimationInfo> animationInfoQueryWrapper = new QueryWrapper<>();
        animationInfoQueryWrapper.eq("deleted",0);
        if(StringUtils.isNotEmpty(keyword)){//搜索
            animationInfoQueryWrapper.like("animation_name",keyword);
        }else{//动画或剧场版列表
            animationInfoQueryWrapper.eq("animation_type",type);
        }
        Page<AnimationInfo> animationInfoPage = animationInfoMapper.selectPage(page, animationInfoQueryWrapper);
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(animationInfoPage.getTotal()+""));
        return DataResult.createBySuccess(animationInfoPage.getRecords(),myPage);
    }

    @Override
    public DataResult getRecommend(Integer limit,Integer userId) {
        //TODO 这里采用的随机查找 这里有条件的可以每天跑一下用户行为MR
        List<AnimationInfoVo> animationInfoVos = animationInfoMapper.getRandomAnimationInfo(limit);
        if(CollectionUtils.isEmpty(animationInfoVos)||userId == null){//用户未登录
            return DataResult.createBySuccess(animationInfoVos);
        }
        List<Integer> animationIds = animationInfoVos.stream().map(AnimationInfoVo::getId).collect(Collectors.toList());
        QueryWrapper<AnimationCollect> animationCollectQueryWrapper = new QueryWrapper<>();
        animationCollectQueryWrapper.eq("user_id",userId).in("animation_id",animationIds);
        List<AnimationCollect> animationCollects = animationCollectMapper.selectList(animationCollectQueryWrapper);
        if(CollectionUtils.isNotEmpty(animationCollects)){
            List<Integer> collected = animationCollects.stream().map(AnimationCollect::getAnimationId).collect(Collectors.toList());
            animationInfoVos.forEach(e ->{
                if(collected.contains(e.getId())){
                    e.setStatus(1);
                }else {
                    e.setStatus(0);
                }
            });
        }

        return DataResult.createBySuccess(animationInfoVos);
    }

    @Override
    public DataResult getDetail(Integer animationId, Integer userId) {
        //调用该接口说明进入了详情页面，该页面视频自动播放，所以播放量加一
        redisTemplate.opsForValue().increment("view_counts_"+animationId);
        //获取详情
        AnimationInfo animationInfo = animationInfoMapper.selectById(animationId);
        AnimationInfoVo animationInfoVo = new AnimationInfoVo();
        BeanUtils.copyProperties(animationInfo,animationInfoVo);
        //判断是否收藏
        if(userId == null){
            //游客访问
            animationInfoVo.setStatus(0);
        }else {
            QueryWrapper<AnimationCollect> animationCollectQueryWrapper = new QueryWrapper<>();
            animationCollectQueryWrapper.eq("user_id",userId).eq("animation_id",animationId);
            List<AnimationCollect> animationCollects = animationCollectMapper.selectList(animationCollectQueryWrapper);
            if(CollectionUtils.isNotEmpty(animationCollects)){
                //有收藏记录
                animationInfoVo.setStatus(1);
            }else {
                animationInfoVo.setStatus(0);
            }
        }
        //获取播放量、弹幕量、装填弹幕
        Object viewCounts = redisTemplate.opsForValue().get("view_counts_" + animationId);
        Object bulletChat = redisTemplate.opsForValue().get("bullet_chat_" + animationId);
        animationInfoVo.setViewCounts(viewCounts==null?0: (Integer) viewCounts);
        animationInfoVo.setBulletChat(bulletChat==null?0: (Integer) bulletChat);
        //获取正片集数
        QueryWrapper<AnimationFile> animationFileQueryWrapper = new QueryWrapper<>();
        animationFileQueryWrapper.eq("animation_id",animationId);
        List<AnimationFile> animationFiles = animationFileMapper.selectList(animationFileQueryWrapper);
        if(CollectionUtils.isEmpty(animationFiles)){
            animationInfoVo.setEpisodeFileVos(new ArrayList<>());
        }else {
            animationInfoVo.setEpisodeFileVos(animationFiles.stream().map(m -> new EpisodeFileVo(m.getEpisodeNo(),m.getFileName())).sorted(Comparator.comparing(EpisodeFileVo::getEpisodeNo)).collect(Collectors.toList()));
        }
        return DataResult.createBySuccess(animationInfoVo);
    }

    @Override
    public DataResult getBulletList(Integer animationId, Integer episodeNo,Integer userId) {
        QueryWrapper<AnimationBullet> bulletQueryWrapper = new QueryWrapper<>();
        bulletQueryWrapper.eq("animation_id",animationId).eq("episode_no",episodeNo).last("limit 1000");;
        List<AnimationBullet> animationBullets = animationBulletMapper.selectList(bulletQueryWrapper);
        if(CollectionUtils.isEmpty(animationBullets)){
            return DataResult.createBySuccess(new ArrayList<>());
        }
        List<AnimationBulletVo> list = new ArrayList<>();
        animationBullets.forEach(e ->{
            AnimationBulletVo bulletVo = new AnimationBulletVo();
            BeanUtils.copyProperties(e,bulletVo);
            if(userId!=null&&e.getUserId() == userId){
                bulletVo.setIsSelf(true);
            }
            bulletVo.setStyle(new HashMap<String,String>(){{put("color",e.getColor());}});
            bulletVo.setDirection(e.getType()==0?"default":"top");
            bulletVo.setTime(e.getSendTime());
            list.add(bulletVo);
        });
        return DataResult.createBySuccess(list);
    }
}
