package com.yiguan.bean.dto;

import com.yiguan.bean.entity.RoleInfo;
import lombok.Data;

/**
 * @Author: lw
 * @CreateTime: 2022-11-15  14:52
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class ChangeRoleDto extends RoleInfo {
    private String originalName;
}
