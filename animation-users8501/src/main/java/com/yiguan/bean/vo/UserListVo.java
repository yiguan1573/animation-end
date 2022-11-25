package com.yiguan.bean.vo;

import com.yiguan.bean.entity.RoleInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: lw
 * @CreateTime: 2022-11-15  13:34
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListVo {
    private Integer id;
    private String userName;
    private String password;
    private List<RoleInfo> roleInfoList;
}
