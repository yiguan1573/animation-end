package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: lw
 * @CreateTime: 2022-10-07  21:08
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleInfo implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    //角色名
    private String roleName;
    //授权服务集合
    private String authorizationList;
}
