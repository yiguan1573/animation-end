package com.yiguan.controller;

import com.alibaba.fastjson.JSON;
import com.yiguan.bean.dto.CarouselDto;
import com.yiguan.bean.entity.*;
import com.yiguan.service.AnimationService;
import com.yiguan.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: lw
 * @CreateTime: 2022-11-16  18:30
 * @Description: TODO 管理员接口
 * @Version: 1.0
 */
@RestController
@RequestMapping("/animation")
@Slf4j
public class AnimationController {
    @Autowired
    MinioUtil minioUtil;
    @Autowired
    AnimationService animationService;
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * @author: lw
     * @date: 2022/11/17 15:01
     * @param: file
     * @param: originUrl
     * @param: id
     * @return: DataResult
     * @description: 上传用户图片
     **/
    @PostMapping("/uploadUserImage")
    public DataResult uploadUserImage(MultipartFile file,String originUrl,Integer id){
        if(file == null||id == null){
            return DataResult.createByError("上传的文件和id不能为空");
        }
        return animationService.uploadUserImage(file,originUrl,id);
    }

    /**
     * @author: lw
     * @date: 2022/11/17 15:01
     * @param: file
     * @param: originUrl
     * @param: id
     * @return: DataResult
     * @description: 上传动画的图片
     **/
    @PostMapping("/uploadImage")
    public DataResult uploadImage(MultipartFile file){
        if(file == null){
            return DataResult.createByError("上传的文件不能为空");
        }
        return animationService.uploadImage(file);
    }

    /**
     * @author: lw
     * @date: 2022/11/17 15:04
     * @param: pageSize
     * @param: pageNo
     * @param: search
     * @return: DataResult
     * @description: 获取动画列表
     **/
    @GetMapping("/getAnimationList")
    public DataResult getAnimationList(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search){
        if (pageNo == null) {
            return DataResult.createByError("pageNo不能为空");
        }
        return animationService.getAnimationList(pageSize,pageNo,search);
    }

    /**
     * @author: lw
     * @date: 2022/11/18 13:59
     * @param: pageSize
     * @param: pageNo
     * @param: search
     * @return: DataResult
     * @description: 走马灯列表选择框里的数据，需要有carouselImageUrl的动画
     **/
    @GetMapping("/carouselSelect")
    public DataResult carouselSelect(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search){
        if (pageNo == null|| StringUtils.isEmpty(search)) {
            return DataResult.createByError("pageNo或关键词不能为空");
        }
        return animationService.carouselSelect(pageSize,pageNo,search);
    }

    /**
     * @author: lw
     * @date: 2022/11/18 13:59
     * @param:
     * @return: DataResult
     * @description: 获取走马灯列表
     **/
    @GetMapping("/getCarouselList")
    public DataResult getCarouselList(){
        return animationService.getCarouselList();
    }

    /**
     * @author: lw
     * @date: 2022/11/18 14:18
     * @param: carouselDto
     * @return: DataResult
     * @description: 添加到走马灯
     **/
    @PostMapping("/addCarousel")
    public  DataResult addCarousel(@RequestBody CarouselDto carouselDto){
        return animationService.addCarousel(carouselDto);
    }

    /**
     * @author: lw
     * @date: 2022/11/17 15:24
     * @param: id
     * @return: DataResult
     * @description: 逻辑删除动画
     **/
    @PostMapping("/deleteAnimation")
    public DataResult deleteAnimation(Integer id){
        if (id == null) {
            return DataResult.createByError("id不能为空");
        }
        return animationService.deleteAnimation(id);
    }

    /**
     * @author: lw
     * @date: 2022/11/17 16:26
     * @param: id
     * @return: DataResult
     * @description: 将逻辑删除的动画上线
     **/
    @PostMapping("/popUpAnimation")
    public DataResult popUpAnimation(Integer id){
        if (id == null) {
            return DataResult.createByError("id不能为空");
        }
        return animationService.popUpAnimation(id);
    }

    /**
     * @author: lw
     * @date: 2022/11/17 16:24
     * @param: animationInfo
     * @return: DataResult
     * @description: 插入或者更新动画
     **/
    @PostMapping("/changeAnimation")
    public DataResult changeAnimation(@Validated @RequestBody AnimationInfo animationInfo){
        return animationService.changeAnimation(animationInfo);
    }

    /**
     * @author: lw
     * @date: 2022/11/18 16:08
     * @param: pageSize
     * @param: pageNo
     * @param: search
     * @return: DataResult
     * @description: 获取动画文件列表
     **/
    @GetMapping("/getFileList")
    public DataResult getFileList(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search){
        if (pageNo == null) {
            return DataResult.createByError("pageNo不能为空");
        }
        return animationService.getFileList(pageSize,pageNo,search);
    }

    /**
     * @author: lw
     * @date: 2022/11/18 16:56
     * @param: id
     * @param: fileName
     * @return: DataResult
     * @description: 删除动画文件
     **/
    @PostMapping("deleteAnimationFile")
    public DataResult deleteAnimationFile(Integer id,String fileName){
        if(id == null||StringUtils.isEmpty(fileName)){
            return DataResult.createByError("id或fileName不能为空");
        }
        return animationService.deleteAnimationFile(id,fileName);
    }

    /**
     * @author: lw
     * @date: 2022/11/18 20:30
     * @param: animationFile
     * @return: DataResult
     * @description: 插入或修改动画文件
     **/
    @PostMapping("/changeAnimationFile")
    public DataResult changeAnimationFile(@RequestBody AnimationFile animationFile){
        return animationService.changeAnimationFile(animationFile);
    }

    /**
     * @author: lw
     * @date: 2022/11/20 11:41
     * @param: uploadTask
     * @return: DataResult
     * @description: 初始化分片上传任务
     **/
    @PostMapping("/createMultipartUpload")
    public  DataResult createMultipartUpload(@RequestBody @Validated UploadTask uploadTask){
        return animationService.createMultipartUpload(uploadTask);
    }

    /**
     * @author: lw
     * @date: 2022/11/20 11:44
     * @param: fileName
     * @param: uploadId
     * @return: DataResult
     * @description: 合并分片
     **/
    @PostMapping("/mergeMultipartUpload")
    public DataResult mergeMultipartUpload(String fileName, String uploadId){
        if(StringUtils.isEmpty(fileName)||StringUtils.isEmpty(uploadId)){
            return DataResult.createByError("fileName或uploadId不能为空");
        }
        return animationService.mergeMultipartUpload(fileName,uploadId);
    }

    @PostMapping("/queryProgress")
    public DataResult queryProgress(String fileName, String uploadId){
        if(StringUtils.isEmpty(fileName)||StringUtils.isEmpty(uploadId)){
            return DataResult.createByError("fileName或uploadId不能为空");
        }
        return animationService.queryProgress(fileName,uploadId);
    }

    /**
     * @author: lw
     * @date: 2022/11/24 20:31
     * @param: animationBullet
     * @return: DataResult
     * @description: 发送系统消息到kafka
     **/
    @PostMapping("/kafka/sendGlobalMessage")
    public DataResult sendGlobalMessage(@Validated @RequestBody AnimationMessage animationMessage) {
        animationMessage.setUpdateTime(System.currentTimeMillis()+"");
        kafkaTemplate.send("globalMessage", JSON.toJSONString(animationMessage)).addCallback(success -> {
            // 消息发送到的topic
            String topic = success.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = success.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = success.getRecordMetadata().offset();
            log.info("发送消息成功:" + topic + "-" + partition + "-" + offset);
        }, failure -> {
            log.error("发送消息失败:" + failure.getMessage());
        });
        return DataResult.createBySuccess();
    }

    /**
     * @author: lw
     * @date: 2022/11/25 17:04
     * @param: id
     * @return: DataResult
     * @description: 删除消息
     **/
    @PostMapping("/deleteMessage")
    DataResult deleteMessage(Integer id){
        if(id == null){
            return DataResult.createByError("id不能为空");
        }
        return animationService.deleteMessage(id);
    }

    /**
     * @author: lw
     * @date: 2022/11/25 17:09
     * @param: animationMessage
     * @return: DataResult
     * @description: 修改系统消息
     **/
    @PostMapping("/changeMessage")
    DataResult changeMessage(@Validated @RequestBody AnimationMessage animationMessage){
        if(animationMessage.getId() == null){
            return DataResult.createByError("id不能为空");
        }
        return animationService.changeMessage(animationMessage);
    }
}
