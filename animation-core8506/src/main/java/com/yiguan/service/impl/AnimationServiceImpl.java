package com.yiguan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yiguan.bean.dto.CarouselDto;
import com.yiguan.bean.entity.*;
import com.yiguan.bean.vo.AnimationFileVo;
import com.yiguan.bean.vo.AnimationInfoVo;
import com.yiguan.bean.vo.ChunkVo;
import com.yiguan.config.MinioConfig;
import com.yiguan.config.MyMinioClient;
import com.yiguan.dao.*;
import com.yiguan.service.AnimationService;
import com.yiguan.util.MinioUtil;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.messages.Part;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-11-17  12:07
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class AnimationServiceImpl implements AnimationService {
    @Autowired
    MinioUtil minioUtil;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    AnimationInfoMapper animationInfoMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AnimationFileMapper animationFileMapper;
    @Autowired
    MyMinioClient minioClient;
    @Autowired
    MinioConfig prop;
    @Autowired
    UploadTaskMapper uploadTaskMapper;
    @Autowired
    AnimationCollectMapper animationCollectMapper;
    @Autowired
    AnimationHistoryMapper animationHistoryMapper;
    @Autowired
    AnimationMessageMapper animationMessageMapper;

    @Override
    public DataResult uploadUserImage(MultipartFile file, String originUrl, Integer id) {
        if(StringUtils.isNotEmpty(originUrl)){
            minioUtil.remove(originUrl);
        }
        String fileName = minioUtil.upload(file);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setImage(fileName);
        userInfoMapper.updateById(userInfo);
        return DataResult.createBySuccess(fileName);
    }

    @Override
    public DataResult uploadImage(MultipartFile file) {
        String fileName = minioUtil.upload(file);
        return DataResult.createBySuccess(fileName);
    }

    @Override
    public DataResult getAnimationList(Integer pageSize, Integer pageNo, String search) {
        Page<AnimationInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<AnimationInfo> animationInfoQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(search)){//增加搜索条件
            animationInfoQueryWrapper.like("animation_name",search).or().like("animation_detail",search);
        }
        Page<AnimationInfo> animationInfoPage = animationInfoMapper.selectPage(page, animationInfoQueryWrapper);
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(animationInfoPage.getTotal()+""));
        List<AnimationInfo> records = animationInfoPage.getRecords();
        ArrayList<AnimationInfoVo> animationInfoVos = new ArrayList<>();
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(animationInfoVos,myPage);
        }
        for (AnimationInfo record : records) {
            AnimationInfoVo animationInfoVo = new AnimationInfoVo();
            BeanUtils.copyProperties(record,animationInfoVo);
            //TODO:到缓存中读取播放量和弹幕量
            Object viewCounts = redisTemplate.opsForValue().get("view_counts_" + record.getId());
            Object bulletChat = redisTemplate.opsForValue().get("bullet_chat_" + record.getId());
            animationInfoVo.setViewCounts(viewCounts==null?0: (Integer) viewCounts);
            animationInfoVo.setBulletChat(bulletChat==null?0: (Integer) bulletChat);
            animationInfoVos.add(animationInfoVo);
        }
        return DataResult.createBySuccess(animationInfoVos,myPage);
    }

    @Override
    public DataResult carouselSelect(Integer pageSize, Integer pageNo, String search) {
        Page<AnimationInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<AnimationInfo> animationInfoQueryWrapper = new QueryWrapper<>();
        animationInfoQueryWrapper.like("animation_name",search).eq("deleted",0);
        Page<AnimationInfo> animationInfoPage = animationInfoMapper.selectPage(page, animationInfoQueryWrapper);
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(animationInfoPage.getTotal()+""));
        List<AnimationInfo> records = animationInfoPage.getRecords();
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(new ArrayList<>(),myPage);
        }
        return DataResult.createBySuccess(records,myPage);
    }

    @Override
    public DataResult getCarouselList() {
        //走马灯只显示4部动画
        ArrayList<AnimationInfo> animationInfos = new ArrayList<>();
        for (int i=1; i<=4; i++){
            Object carousel = redisTemplate.opsForValue().get("carousel_" + i);
            if(carousel == null){
                animationInfos.add(new AnimationInfo());
            }else {
                animationInfos.add(JSON.parseObject(carousel.toString(),AnimationInfo.class));
            }
        }
        return DataResult.createBySuccess(animationInfos);
    }

    @Override
    public DataResult addCarousel(CarouselDto carouselDto) {
        AnimationInfo animationInfo = new AnimationInfo();
        BeanUtils.copyProperties(carouselDto,animationInfo);
        redisTemplate.opsForValue().set("carousel_"+carouselDto.getIndex(),JSON.toJSONString(animationInfo));
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult deleteAnimation(Integer id) {
        AnimationInfo animationInfo = new AnimationInfo();
        animationInfo.setId(id);
        animationInfo.setDeleted(1);
        animationInfoMapper.updateById(animationInfo);
        //TODO:收藏表和观看记录表的标志位改为1
        UpdateWrapper<AnimationCollect> animationCollectUpdateWrapper = new UpdateWrapper<>();
        animationCollectUpdateWrapper.eq("animation_id",id).set("deleted",1);
        animationCollectMapper.update(null,animationCollectUpdateWrapper);

        UpdateWrapper<AnimationHistory> animationHistoryUpdateWrapper = new UpdateWrapper<>();
        animationCollectUpdateWrapper.eq("animation_id",id).set("deleted",1);
        animationHistoryMapper.update(null,animationHistoryUpdateWrapper);

        return DataResult.createBySuccess();
    }

    @Override
    public DataResult popUpAnimation(Integer id) {
        AnimationInfo animationInfo = new AnimationInfo();
        animationInfo.setId(id);
        animationInfo.setDeleted(0);
        animationInfoMapper.updateById(animationInfo);
        //TODO:收藏表和观看记录表的标志位改为0
        UpdateWrapper<AnimationCollect> animationCollectUpdateWrapper = new UpdateWrapper<>();
        animationCollectUpdateWrapper.eq("animation_id",id).set("deleted",0);
        animationCollectMapper.update(null,animationCollectUpdateWrapper);

        UpdateWrapper<AnimationHistory> animationHistoryUpdateWrapper = new UpdateWrapper<>();
        animationCollectUpdateWrapper.eq("animation_id",id).set("deleted",0);
        animationHistoryMapper.update(null,animationHistoryUpdateWrapper);

        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeAnimation(AnimationInfo animationInfo) {
        if(animationInfo.getId() == null){//插入数据
            animationInfoMapper.insert(animationInfo);
            //在缓存中加入播放量和弹幕量
            redisTemplate.opsForValue().set("view_counts_"+animationInfo.getId(),0);
            redisTemplate.opsForValue().set("bullet_chat_"+animationInfo.getId(),0);
            return DataResult.createBySuccess("插入成功");
        }
        animationInfoMapper.updateById(animationInfo);
        return DataResult.createBySuccess("更新成功");
    }

    @Override
    public DataResult getFileList(Integer pageSize, Integer pageNo, String search) {
        Integer totalFile = animationFileMapper.getTotalFile(search);
        MyPage myPage = new MyPage(pageNo, pageSize, totalFile);
        List<AnimationFileVo> fileList = animationFileMapper.getFileList(pageSize, pageNo, search);
        return DataResult.createBySuccess(fileList,myPage);
    }

    @Override
    public DataResult deleteAnimationFile(Integer id, String fileName) {
        //删除animation_file表中的记录
        animationFileMapper.deleteById(id);
        //删除uploadTask记录
        QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
        uploadTaskQueryWrapper.eq("file_name",fileName);
        uploadTaskMapper.delete(uploadTaskQueryWrapper);
        //删除文件
        minioUtil.remove(fileName);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeAnimationFile(AnimationFile animationFile) {
        QueryWrapper<AnimationFile> animationFileQueryWrapper = new QueryWrapper<>();
        animationFileQueryWrapper.eq("animation_id",animationFile.getAnimationId())
                .eq("episode_no",animationFile.getEpisodeNo());
        if(animationFile.getId() == null){
            List<AnimationFile> animationFiles = animationFileMapper.selectList(animationFileQueryWrapper);
            if(CollectionUtils.isNotEmpty(animationFiles)){
                return deleteReetition(animationFileQueryWrapper,animationFile);
            }else {
                //插入
                animationFileMapper.insert(animationFile);
            }
        }
        animationFileQueryWrapper.ne("id",animationFile.getId());
        List<AnimationFile> animationFiles = animationFileMapper.selectList(animationFileQueryWrapper);
        if(CollectionUtils.isNotEmpty(animationFiles)){
            return deleteReetition(animationFileQueryWrapper,animationFile);
        }
        animationFileMapper.updateById(animationFile);
        return DataResult.createBySuccess();
    }

    DataResult deleteReetition(QueryWrapper<AnimationFile> animationFileQueryWrapper,AnimationFile animationFile){
        animationFileQueryWrapper.clear();
        animationFileQueryWrapper.eq("file_name",animationFile.getFileName());
        List<AnimationFile> animationFiles1 = animationFileMapper.selectList(animationFileQueryWrapper);
        if(CollectionUtils.isEmpty(animationFiles1)){
            //删除uploadTask记录
            QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
            uploadTaskQueryWrapper.eq("file_name",animationFile.getFileName());
            uploadTaskMapper.delete(uploadTaskQueryWrapper);
            //删除刚刚上传的文件
            minioUtil.remove(animationFile.getFileName());
        }
        return DataResult.createByError("本集已经上传");
    }

    @SneakyThrows
    @Override
    public DataResult createMultipartUpload(UploadTask uploadTask) {

        Map<String, Object> result = new HashMap<>();
        String originalFilename = uploadTask.getFileName();
        String newFileName = originalFilename.substring(0,originalFilename.lastIndexOf("."))+"."+ UuidUtils.generateUuid().replaceAll("-","") + originalFilename.substring(originalFilename.lastIndexOf("."));
        ArrayList<ChunkVo> updateList = new ArrayList<>();//需要上传的分片

        Map<String, String> reqParams = new HashMap<>();

        //根据文件md5查看是否已经上传完成
        QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
        uploadTaskQueryWrapper.eq("md5",uploadTask.getMd5());
        List<UploadTask> uploadTasks = uploadTaskMapper.selectList(uploadTaskQueryWrapper);
        if(CollectionUtils.isNotEmpty(uploadTasks)){
            result.put("uploadId", uploadTasks.get(0).getUploadId());
            result.put("newFileName",uploadTasks.get(0).getFileName());
            if(uploadTasks.get(0).getStatus() == 1){//已经上传过了，实现秒传
                result.put("status",1);
                return DataResult.createBySuccess(result);
            }else {//还在上传中，实现断点续传

                ListPartsResponse listPartsResponse = minioClient.listMultipart(prop.getBucketName(), null, uploadTasks.get(0).getFileName(), null, null, uploadTasks.get(0).getUploadId(), null, null);
                //已经上传的分片id
                List<Integer> uploaded = listPartsResponse.result().partList().stream().map(Part::partNumber).collect(Collectors.toList());
                if(uploaded.size() == uploadTask.getChunkNum()){
                    //分片已经上传完，只差合并
                    result.put("status",-1);
                    return DataResult.createBySuccess(result);
                }
                reqParams.put("uploadId", uploadTasks.get(0).getUploadId());
                //从1开始生成的url才有效
                for (int i = 1; i <= uploadTask.getChunkNum(); i++) {
                    if(!uploaded.contains(i)){
                        reqParams.put("partNumber", String.valueOf(i));
                        String uploadUrl = minioClient.getPresignedObjectUrl(prop.getBucketName(), newFileName, reqParams);// 获取URL
                        updateList.add(new ChunkVo(i,uploadUrl));
                    }
                }
                result.put("status",0);
                result.put("needUpdate",updateList);
                return DataResult.createBySuccess(result);
            }
        }

        // 根据文件名创建签名
        CreateMultipartUploadResponse response = minioClient.uploadId(prop.getBucketName(), null, newFileName, null, null);
        // 获取uploadId
        String uploadId = response.result().uploadId();
        result.put("uploadId", uploadId);
        uploadTask.setUploadId(uploadId);
        uploadTask.setBucketName(prop.getBucketName());
        uploadTask.setFileName(newFileName);
        uploadTask.setStatus(0);
        result.put("newFileName",newFileName);
        result.put("status",0);//正在上传
        // 请求Minio 服务，获取每个分块带签名的上传URL
        reqParams.put("uploadId", uploadId);
        // 循环分块数
        for (int i = 1; i <= uploadTask.getChunkNum(); i++) {
            reqParams.put("partNumber", String.valueOf(i));
            String uploadUrl = minioClient.getPresignedObjectUrl(prop.getBucketName(), newFileName, reqParams);// 获取URL
            updateList.add(new ChunkVo(i,uploadUrl));
        }
        result.put("needUpdate",updateList);
        //保存数据库
        uploadTaskMapper.insert(uploadTask);
        return DataResult.createBySuccess(result);
    }

    @Override
    public DataResult mergeMultipartUpload(String fileName, String uploadId) {
        try {
            Part[] parts = new Part[1000];
            //此方法注意2020.02.04之前的minio服务端有bug
            ListPartsResponse partResult = minioClient.listMultipart(prop.getBucketName(), null, fileName, 1000, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            minioClient.completeMultipartUpload(prop.getBucketName(), null, fileName, uploadId, parts, null, null);
            //更新状态
            UpdateWrapper<UploadTask> uploadTaskUpdateWrapper = new UpdateWrapper<>();
            uploadTaskUpdateWrapper.eq("file_name",fileName).eq("upload_id",uploadId).set("status",1);
            uploadTaskMapper.update(null,uploadTaskUpdateWrapper);
            //TODO: 合并成功后生成不同分辨率的视频文件

        } catch (Exception e) {
            log.error("error: {}", e.getMessage(), e);
            return DataResult.createByError();
        }

        return DataResult.createBySuccess();
    }

    @Override
    public DataResult queryProgress(String fileName, String uploadId) {
        try {
            ListPartsResponse partResult = minioClient.listMultipart(prop.getBucketName(), null, fileName, 1000, 0, uploadId, null, null);
            List<Part> parts = partResult.result().partList();
            return DataResult.createBySuccess(parts);
        } catch (Exception e) {
            e.printStackTrace();
            return DataResult.createByError();
        }
    }

    @Override
    public DataResult deleteMessage(Integer id) {
        animationMessageMapper.deleteById(id);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeMessage(AnimationMessage animationMessage) {
        animationMessageMapper.updateById(animationMessage);
        return DataResult.createBySuccess();
    }
}
