package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: lw
 * @CreateTime: 2022-10-07  21:10
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationInfo implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private String authorizationName;
    private String authorizationUrl;
}
