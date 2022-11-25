package com.yiguan.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: lw
 * @CreateTime: 2022-10-07  21:04
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    //用户名
    private String userName;
    //加密后的密码
    private String password;
    //用户头像
    private String image;
    //角色集合
    private String roleList;
}
