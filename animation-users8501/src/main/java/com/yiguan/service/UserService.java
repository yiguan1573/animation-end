package com.yiguan.service;

import com.yiguan.bean.dto.ChangeRoleDto;
import com.yiguan.bean.entity.AuthorizationInfo;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.RoleInfo;
import com.yiguan.bean.entity.UserInfo;

public interface UserService {
    DataResult getMessage(Integer id);

    DataResult register(String username,String password);

    DataResult changeData(UserInfo userInfo);

    DataResult changeRole(ChangeRoleDto roleInfo);

    DataResult changeAuthorization(AuthorizationInfo authorizationInfo);

    DataResult getUserList(Integer pageSize,Integer pageNo,String search);

    DataResult getRoleList(Integer pageSize,Integer pageNo,String search);

    DataResult getAuthorizationList(Integer pageSize,Integer pageNo,String search);

    DataResult addUser(UserInfo userInfo);

    DataResult addRole(RoleInfo roleInfo);

    DataResult addAuthorization(AuthorizationInfo authorizationInfo);

    DataResult deleteUser(Integer id);

    DataResult deleteRole(Integer id,String roleName);

    DataResult deleteAuthorization(Integer id);
}
