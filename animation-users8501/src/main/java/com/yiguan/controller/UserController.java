package com.yiguan.controller;

import com.yiguan.bean.dto.ChangeRoleDto;
import com.yiguan.bean.entity.AuthorizationInfo;
import com.yiguan.bean.entity.DataResult;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lw
 * @CreateTime: 2022-11-13  18:07
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    /**
     * @author: lw
     * @date: 2022/11/13 19:03
     * @param: username
     * @return: DataResult
     * @description: 获取用户基本信息
     **/
    @PostMapping("/getMessage")
    public DataResult getMessage(Integer id) {
        return userService.getMessage(id);
    }

    /**
     * @author: lw
     * @date: 2022/11/13 19:05
     * @param: username
     * @param: password
     * @return: DataResult
     * @description: 注册，这里偷懒后端没有加用户名和密码的校验
     **/
    @PostMapping("/register")
    public DataResult register(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return DataResult.createByError("用户名或密码不能为空");
        }
        return userService.register(username, password);
    }

    /**
     * @author: lw
     * @date: 2022/11/14 19:07
     * @param: username
     * @param: password
     * @param: id
     * @return: DataResult
     * @description: 修改用户资料
     **/
    @PostMapping("/changeData")
    public DataResult changeData(@RequestBody UserInfo userInfo) {
        if (userInfo.getId() == null) {//插入
            if (StringUtils.isEmpty(userInfo.getUserName()) || StringUtils.isEmpty(userInfo.getPassword()) || StringUtils.isEmpty(userInfo.getRoleList())) {
                return DataResult.createByError("用户名、密码或角色列表不能为空");
            }
            return userService.addUser(userInfo);
        }

        if (StringUtils.isEmpty(userInfo.getUserName()) || StringUtils.isEmpty(userInfo.getPassword())) {
            return DataResult.createByError("用户名或密码不能为空");
        }

        return userService.changeData(userInfo);
    }

    /**
     * @author: lw
     * @date: 2022/11/14 19:07
     * @param: username
     * @param: password
     * @param: id
     * @return: DataResult
     * @description: 修改用户资料
     **/
    @PostMapping("/changeRole")
    public DataResult changeRole(@RequestBody ChangeRoleDto roleInfo) {
        if (roleInfo.getId() == null) {//插入
            if (StringUtils.isEmpty(roleInfo.getRoleName()) || StringUtils.isEmpty(roleInfo.getAuthorizationList())) {
                return DataResult.createByError("角色名或权限列表不能为空");
            }
            return userService.addRole(roleInfo);
        }

        if (StringUtils.isEmpty(roleInfo.getOriginalName()) || StringUtils.isEmpty(roleInfo.getRoleName())) {
            return DataResult.createByError("角色名不能为空");
        }

        return userService.changeRole(roleInfo);
    }

    /**
     * @author: lw
     * @date: 2022/11/14 19:07
     * @param: username
     * @param: password
     * @param: id
     * @return: DataResult
     * @description: 修改用户资料
     **/
    @PostMapping("/changeAuthorization")
    public DataResult changeAuthorization(@RequestBody AuthorizationInfo authorizationInfo) {
        if (authorizationInfo.getId() == null) {
            if (StringUtils.isEmpty(authorizationInfo.getAuthorizationName()) || StringUtils.isEmpty(authorizationInfo.getAuthorizationUrl())) {
                return DataResult.createByError("权限名或权限路径不能为空");
            }
            return userService.addAuthorization(authorizationInfo);
        }

        if (StringUtils.isEmpty(authorizationInfo.getAuthorizationName()) || StringUtils.isEmpty(authorizationInfo.getAuthorizationUrl()) || authorizationInfo.getId() == null) {
            return DataResult.createByError("角色名不能为空");
        }
        return userService.changeAuthorization(authorizationInfo);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 13:19
     * @param: pageSize
     * @param: pageNo
     * @return: DataResult
     * @description: 获取用户列表
     **/
    @GetMapping("/getUserList")
    public DataResult getUserList(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search) {
        if (pageNo == null) {
            return DataResult.createByError("pageNo不能为空");
        }
        return userService.getUserList(pageSize, pageNo, search);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 13:19
     * @param: pageSize
     * @param: pageNo
     * @return: DataResult
     * @description: 获取角色列表
     **/
    @GetMapping("/getRoleList")
    public DataResult getRoleList(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search) {
        if (pageNo == null) {
            return DataResult.createByError("pageNo不能为空");
        }
        return userService.getRoleList(pageSize, pageNo, search);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 13:19
     * @param: pageSize
     * @param: pageNo
     * @return: DataResult
     * @description: 获取权限列表getAuthorizationList
     **/
    @GetMapping("/getAuthorizationList")
    public DataResult getAuthorizationList(@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, Integer pageNo, String search) {
        if (pageNo == null) {
            return DataResult.createByError("pageNo不能为空");
        }
        return userService.getAuthorizationList(pageSize, pageNo, search);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 18:52
     * @param: id
     * @return: DataResult
     * @description:
     **/
    @PostMapping("/deleteUser")
    public DataResult deleteUser(Integer id) {
        if (id == null) {
            return DataResult.createByError("id不能为空");
        }
        return userService.deleteUser(id);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 18:52
     * @param: id
     * @return: DataResult
     * @description:
     **/
    @PostMapping("/deleteRole")
    public DataResult deleteRole(Integer id, String roleName) {
        if (id == null||StringUtils.isEmpty(roleName)) {
            return DataResult.createByError("id或角色名不能为空");
        }
        return userService.deleteRole(id,roleName);
    }

    /**
     * @author: lw
     * @date: 2022/11/15 18:52
     * @param: id
     * @return: DataResult
     * @description:
     **/
    @PostMapping("/deleteAuthorization")
    public DataResult deleteAuthorization(Integer id) {
        if (id == null) {
            return DataResult.createByError("id不能为空");
        }
        return userService.deleteAuthorization(id);
    }

}


