package com.yiguan.bean.vo;

import com.yiguan.bean.entity.AuthorizationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: lw
 * @CreateTime: 2022-11-15  13:57
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleListVo {
    private Integer id;
    private String roleName;
    private List<AuthorizationInfo> authorizationInfoList;
}
