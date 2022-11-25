package com.yiguan.controller;

import com.alibaba.fastjson.JSON;
import com.yiguan.bean.entity.AnimationBullet;
import com.yiguan.bean.entity.AnimationHistory;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.service.CoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lw
 * @CreateTime: 2022-11-21  16:58
 * @Description: TODO 需要用户登录才能访问
 * @Version: 1.0
 */
@RestController
@RequestMapping("/core")
@Slf4j
public class CoreController {
    @Autowired
    CoreService coreService;
    //虽然报红，但还是能用
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * @author: lw
     * @date: 2022/11/21 17:06
     * @param: status 0代表取消收藏，1代表收藏
     * @param: userId
     * @param: animationId
     * @return: DataResult
     * @description: 收藏
     **/
    @PostMapping("/collect")
    DataResult collect(Integer status,Integer userId,Integer animationId){
        if(status==null||userId==null||animationId==null){
            return DataResult.createByError("参数不能为空");
        }
        return coreService.collect(status,userId,animationId);
    }

    /**
     * @author: lw
     * @date: 2022/11/21 21:14
     * @param: pageNo
     * @param: pageSize
     * @param: keyword
     * @param: userId
     * @return: DataResult
     * @description: 获取收藏列表
     **/
    @GetMapping("getCollectList")
    DataResult getCollectList(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "12") Integer pageSize, String keyword, Integer userId){
        if(userId == null){
            return DataResult.createByError("userId不能为空");
        }
        return coreService.getCollectList(pageNo,pageSize,keyword,userId);
    }

    /**
     * @author: lw
     * @date: 2022/11/22 15:02
     * @param: animationHistory
     * @return: DataResult
     * @description: 记录历史信息
     **/
    @PostMapping("/recordHistory")
    DataResult recordHistory(@RequestBody @Validated AnimationHistory animationHistory){
        return coreService.recordHistory(animationHistory);
    }

    /**
     * @author: lw
     * @date: 2022/11/22 15:03
     * @param: pageNo
     * @param: pageSize
     * @param: keyword
     * @param: userId
     * @return: DataResult
     * @description: 获取历史记录
     **/
    @GetMapping("/getHistoryList")
    DataResult getHistoryList(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "4") Integer pageSize, String keyword, Integer userId){
        if(userId == null){
            return DataResult.createByError("userId不能为空");
        }
        return coreService.getHistoryList(pageNo,pageSize,keyword,userId);
    }

    /**
     * @author: lw
     * @date: 2022/11/22 15:03
     * @param: id
     * @param: userId
     * @return: DataResult
     * @description: 删除历史记录，当userId不为空则清空该用户所有的历史记录，否则只删除单条记录
     **/
    @PostMapping("/deleteHistory")
    DataResult deleteHistory(Integer id,Integer userId){
        if(id == null&&userId == null){
            return DataResult.createByError("id和userId不能都为空");
        }
        return coreService.deleteHistory(id,userId);
    }

    /**
     * @author: lw
     * @date: 2022/11/24 20:31
     * @param: animationBullet
     * @return: DataResult
     * @description: 发送弹幕到kafka
     **/
    @PostMapping("/kafka/sendBullet")
    public DataResult sendBullet(@RequestBody AnimationBullet animationBullet) {
        kafkaTemplate.send("bullet", JSON.toJSONString(animationBullet)).addCallback(success -> {
            // 消息发送到的topic
            String topic = success.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = success.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = success.getRecordMetadata().offset();
            log.info("发送消息成功:" + topic + "-" + partition + "-" + offset);
            //增加redis中弹幕数量
            redisTemplate.opsForValue().increment("bullet_chat_" + animationBullet.getAnimationId());
        }, failure -> {
            log.error("发送消息失败:" + failure.getMessage());
        });
        return DataResult.createBySuccess();
    }

    @GetMapping("/getGlobalMessage")
    DataResult getGlobalMessage(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize, String keyword){
        return coreService.getGlobalMessage(pageNo,pageSize,keyword);
    }


}
