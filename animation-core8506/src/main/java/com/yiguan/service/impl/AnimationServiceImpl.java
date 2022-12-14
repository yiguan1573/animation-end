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
        if(StringUtils.isNotEmpty(search)){//??????????????????
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
            //TODO:???????????????????????????????????????
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
        //??????????????????4?????????
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
        //TODO:?????????????????????????????????????????????1
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
        //TODO:?????????????????????????????????????????????0
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
        if(animationInfo.getId() == null){//????????????
            animationInfoMapper.insert(animationInfo);
            //???????????????????????????????????????
            redisTemplate.opsForValue().set("view_counts_"+animationInfo.getId(),0);
            redisTemplate.opsForValue().set("bullet_chat_"+animationInfo.getId(),0);
            return DataResult.createBySuccess("????????????");
        }
        animationInfoMapper.updateById(animationInfo);
        return DataResult.createBySuccess("????????????");
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
        //??????animation_file???????????????
        animationFileMapper.deleteById(id);
        //??????uploadTask??????
        QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
        uploadTaskQueryWrapper.eq("file_name",fileName);
        uploadTaskMapper.delete(uploadTaskQueryWrapper);
        //????????????
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
                //??????
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
            //??????uploadTask??????
            QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
            uploadTaskQueryWrapper.eq("file_name",animationFile.getFileName());
            uploadTaskMapper.delete(uploadTaskQueryWrapper);
            //???????????????????????????
            minioUtil.remove(animationFile.getFileName());
        }
        return DataResult.createByError("??????????????????");
    }

    @SneakyThrows
    @Override
    public DataResult createMultipartUpload(UploadTask uploadTask) {

        Map<String, Object> result = new HashMap<>();
        String originalFilename = uploadTask.getFileName();
        String newFileName = originalFilename.substring(0,originalFilename.lastIndexOf("."))+"."+ UuidUtils.generateUuid().replaceAll("-","") + originalFilename.substring(originalFilename.lastIndexOf("."));
        ArrayList<ChunkVo> updateList = new ArrayList<>();//?????????????????????

        Map<String, String> reqParams = new HashMap<>();

        //????????????md5??????????????????????????????
        QueryWrapper<UploadTask> uploadTaskQueryWrapper = new QueryWrapper<>();
        uploadTaskQueryWrapper.eq("md5",uploadTask.getMd5());
        List<UploadTask> uploadTasks = uploadTaskMapper.selectList(uploadTaskQueryWrapper);
        if(CollectionUtils.isNotEmpty(uploadTasks)){
            result.put("uploadId", uploadTasks.get(0).getUploadId());
            result.put("newFileName",uploadTasks.get(0).getFileName());
            if(uploadTasks.get(0).getStatus() == 1){//?????????????????????????????????
                result.put("status",1);
                return DataResult.createBySuccess(result);
            }else {//????????????????????????????????????

                ListPartsResponse listPartsResponse = minioClient.listMultipart(prop.getBucketName(), null, uploadTasks.get(0).getFileName(), null, null, uploadTasks.get(0).getUploadId(), null, null);
                //?????????????????????id
                List<Integer> uploaded = listPartsResponse.result().partList().stream().map(Part::partNumber).collect(Collectors.toList());
                if(uploaded.size() == uploadTask.getChunkNum()){
                    //????????????????????????????????????
                    result.put("status",-1);
                    return DataResult.createBySuccess(result);
                }
                reqParams.put("uploadId", uploadTasks.get(0).getUploadId());
                //???1???????????????url?????????
                for (int i = 1; i <= uploadTask.getChunkNum(); i++) {
                    if(!uploaded.contains(i)){
                        reqParams.put("partNumber", String.valueOf(i));
                        String uploadUrl = minioClient.getPresignedObjectUrl(prop.getBucketName(), newFileName, reqParams);// ??????URL
                        updateList.add(new ChunkVo(i,uploadUrl));
                    }
                }
                result.put("status",0);
                result.put("needUpdate",updateList);
                return DataResult.createBySuccess(result);
            }
        }

        // ???????????????????????????
        CreateMultipartUploadResponse response = minioClient.uploadId(prop.getBucketName(), null, newFileName, null, null);
        // ??????uploadId
        String uploadId = response.result().uploadId();
        result.put("uploadId", uploadId);
        uploadTask.setUploadId(uploadId);
        uploadTask.setBucketName(prop.getBucketName());
        uploadTask.setFileName(newFileName);
        uploadTask.setStatus(0);
        result.put("newFileName",newFileName);
        result.put("status",0);//????????????
        // ??????Minio ?????????????????????????????????????????????URL
        reqParams.put("uploadId", uploadId);
        // ???????????????
        for (int i = 1; i <= uploadTask.getChunkNum(); i++) {
            reqParams.put("partNumber", String.valueOf(i));
            String uploadUrl = minioClient.getPresignedObjectUrl(prop.getBucketName(), newFileName, reqParams);// ??????URL
            updateList.add(new ChunkVo(i,uploadUrl));
        }
        result.put("needUpdate",updateList);
        //???????????????
        uploadTaskMapper.insert(uploadTask);
        return DataResult.createBySuccess(result);
    }

    @Override
    public DataResult mergeMultipartUpload(String fileName, String uploadId) {
        try {
            Part[] parts = new Part[1000];
            //???????????????2020.02.04?????????minio????????????bug
            ListPartsResponse partResult = minioClient.listMultipart(prop.getBucketName(), null, fileName, 1000, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            minioClient.completeMultipartUpload(prop.getBucketName(), null, fileName, uploadId, parts, null, null);
            //????????????
            UpdateWrapper<UploadTask> uploadTaskUpdateWrapper = new UpdateWrapper<>();
            uploadTaskUpdateWrapper.eq("file_name",fileName).eq("upload_id",uploadId).set("status",1);
            uploadTaskMapper.update(null,uploadTaskUpdateWrapper);
            //TODO: ???????????????????????????????????????????????????

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
