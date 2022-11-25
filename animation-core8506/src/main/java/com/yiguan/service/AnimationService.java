package com.yiguan.service;

import com.yiguan.bean.dto.CarouselDto;
import com.yiguan.bean.entity.*;
import org.springframework.web.multipart.MultipartFile;

public interface AnimationService {
    DataResult uploadUserImage(MultipartFile file, String originUrl, Integer id);

    DataResult uploadImage(MultipartFile file);

    DataResult getAnimationList(Integer pageSize, Integer pageNo, String search);

    DataResult carouselSelect(Integer pageSize, Integer pageNo, String search);

    DataResult getCarouselList();

    DataResult addCarousel(CarouselDto carouselDto);

    DataResult deleteAnimation(Integer id);

    DataResult popUpAnimation(Integer id);

    DataResult changeAnimation(AnimationInfo animationInfo);

    DataResult getFileList(Integer pageSize, Integer pageNo, String search);

    DataResult deleteAnimationFile(Integer id,String fileName);

    DataResult changeAnimationFile(AnimationFile animationFile);

    DataResult createMultipartUpload(UploadTask uploadTask);

    DataResult mergeMultipartUpload(String fileName, String uploadId);

    DataResult queryProgress(String fileName, String uploadId);

    DataResult deleteMessage(Integer id);

    DataResult changeMessage(AnimationMessage animationMessage);
}
