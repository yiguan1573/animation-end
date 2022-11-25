package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: lw
 * @CreateTime: 2022-11-19  17:36
 * @Description: TODO 分片上传表
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadTask {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String uploadId;
    @NotBlank
    private String md5;
    @NotBlank
    private String fileName;
    private String bucketName;
    @NotNull
    private Long totalSize;
    @NotNull
    private Long chunkSize;
    @NotNull
    private Integer chunkNum;
    private Integer status;
}
