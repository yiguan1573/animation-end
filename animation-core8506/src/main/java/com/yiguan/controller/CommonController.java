package com.yiguan.controller;

import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.MyPage;
import com.yiguan.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lw
 * @CreateTime: 2022-11-21  14:14
 * @Description: TODO 游客可以访问的接口
 * @Version: 1.0
 */
@RestController
@RequestMapping("/common")
public class CommonController {
    @Autowired
    CommonService commonService;

    /**
     * @author: lw
     * @date: 2022/11/21 14:48
     * @param: pageNo
     * @param: pageSize
     * @param: keyword
     * @param: type 0代表动画，1代表剧场版
     * @return: DataResult
     * @description:
     **/
    @GetMapping("/commonAnimationList")
    DataResult commonAnimationList(@RequestParam(defaultValue = "1") Integer pageNo,@RequestParam(defaultValue = "14") Integer pageSize, String keyword,@RequestParam(defaultValue = "0") Integer type){
        return commonService.commonAnimationList(pageNo,pageSize,keyword,type);
    }

    /**
     * @author: lw
     * @date: 2022/11/21 16:43
     * @param: limit
     * @param: userId
     * @return: DataResult
     * @description: 推荐动画
     **/
    @GetMapping("/getRecommend")
    DataResult getDaylongList(Integer limit,Integer userId){
        return commonService.getRecommend(limit,userId);
    }

    /**
     * @author: lw
     * @date: 2022/11/21 17:16
     * @param: animationId
     * @param: userId
     * @return: DataResult
     * @description: 获取动画详情
     **/
    @GetMapping("/getDetail")
    DataResult getDetail(Integer animationId,Integer userId){
        if (animationId == null){
            return DataResult.createByError("animationId不能为空");
        }
        return commonService.getDetail(animationId,userId);
    }

    /**
     * @author: lw
     * @date: 2022/11/24 20:34
     * @param: animationId
     * @param: episodeNo
     * @return: DataResult
     * @description: 获取弹幕
     **/
    @GetMapping("/getBulletList")
    DataResult getBulletList(Integer animationId,Integer episodeNo,Integer userId){
        if(animationId == null||episodeNo == null){
            return DataResult.createByError("animationId或episodeNo不能为空");
        }
        return commonService.getBulletList(animationId,episodeNo,userId);
    }
}
