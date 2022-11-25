package com.yiguan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yiguan.bean.dto.ChangeRoleDto;
import com.yiguan.bean.entity.*;
import com.yiguan.bean.vo.RoleListVo;
import com.yiguan.bean.vo.UserListVo;
import com.yiguan.dao.AuthorizationInfoMapper;
import com.yiguan.dao.RoleInfoMapper;
import com.yiguan.dao.UserInfoMapper;
import com.yiguan.service.UserService;
import com.yiguan.util.Oauth2Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-11-13  18:56
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    RoleInfoMapper roleInfoMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    Oauth2Utils oauth2Utils;
    @Autowired
    AuthorizationInfoMapper authorizationInfoMapper;

    private static final ExecutorService SINGLE_THREAD_POOL = Executors.newSingleThreadExecutor();

    @Override
    public DataResult getMessage(Integer id) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("id",id);
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if(CollectionUtil.isEmpty(userInfos)){
            return DataResult.createByError("该用户名不存在");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("image",userInfos.get(0).getImage());
        map.put("username",userInfos.get(0).getUserName());
        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
        roleInfoQueryWrapper.in("id", userInfos.get(0).getRoleList().split(","));
        List<RoleInfo> roleInfos = roleInfoMapper.selectList(roleInfoQueryWrapper);
        if(CollectionUtils.isNotEmpty(roleInfos)){
            map.put("role",roleInfos.stream().map(RoleInfo::getRoleName).collect(Collectors.toList()));
        }else {
            map.put("role","ROLE_COMMON");
        }
        map.put("id",id);
        Object object = redisTemplate.opsForValue().get("access_token_" + id);
        if(object!=null){
            map.put("token",object.toString());
        }else {
            map.put("token",null);
        }
        SINGLE_THREAD_POOL.execute(() -> {
            if(id != null){
                //刷新缓存中的权限信息
                oauth2Utils.refreshCash(id);
            }
        });


        return  DataResult.createBySuccess(map);
    }

    @Override
    public DataResult register(String username, String password) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("user_name",username);
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if(CollectionUtil.isNotEmpty(userInfos)){
            return DataResult.createByError("该用户名已存在");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(password);
        userInfo.setUserName(username);
        //默认角色 普通
        userInfo.setRoleList("2");
        userInfoMapper.insert(userInfo);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeData(UserInfo userInfo) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("user_name",userInfo.getUserName()).ne("id",userInfo.getId());
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        if(CollectionUtils.isNotEmpty(userInfos)){
            return DataResult.createByError("该用户名已存在");
        }
        userInfoMapper.updateById(userInfo);
        //更新缓存
        oauth2Utils.refreshCash(userInfo.getId());
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeRole(ChangeRoleDto roleInfo) {
        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
        roleInfoQueryWrapper.eq("role_name",roleInfo.getRoleName()).ne("id",roleInfo.getId());
        List<RoleInfo> roleInfos = roleInfoMapper.selectList(roleInfoQueryWrapper);
        if(CollectionUtils.isNotEmpty(roleInfos)){
            return DataResult.createByError("该角色名已存在");
        }
        roleInfoMapper.updateById(roleInfo);
        //更新缓存
        redisTemplate.delete(roleInfo.getOriginalName());
        QueryWrapper<AuthorizationInfo> authorizationInfoQueryWrapper = new QueryWrapper<>();
        authorizationInfoQueryWrapper.in("id",roleInfo.getAuthorizationList().split(","));
        List<AuthorizationInfo> authorizationInfos = authorizationInfoMapper.selectList(authorizationInfoQueryWrapper);
        redisTemplate.opsForValue().set(roleInfo.getRoleName(),authorizationInfos.stream().map(m->m.getId().toString()).collect(Collectors.joining(",")));

        return DataResult.createBySuccess();
    }

    @Override
    public DataResult changeAuthorization(AuthorizationInfo authorizationInfo) {
        QueryWrapper<AuthorizationInfo> authorizationInfoQueryWrapper = new QueryWrapper<>();
        authorizationInfoQueryWrapper.eq("authorization_name",authorizationInfo.getAuthorizationName()).ne("id",authorizationInfo.getId());
        List<AuthorizationInfo> authorizationInfos = authorizationInfoMapper.selectList(authorizationInfoQueryWrapper);
        if(CollectionUtils.isNotEmpty(authorizationInfos)){
            return DataResult.createByError("该权限名已存在");
        }
        authorizationInfoMapper.updateById(authorizationInfo);
        //更新缓存
        redisTemplate.opsForValue().set("authorization_"+authorizationInfo.getId(),authorizationInfo.getAuthorizationUrl());
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult getUserList(Integer pageSize, Integer pageNo,String search) {
        Page<UserInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(search)){//增加搜索条件
            userInfoQueryWrapper.like("user_name",search);
        }
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(page, userInfoQueryWrapper);


        List<UserInfo> records = userInfoPage.getRecords();
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(userInfoPage.getTotal()+""));
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(new ArrayList<>(),myPage);
        }
        List<String> roleIds = records.stream().map(m -> m.getRoleList()).flatMap(e -> Arrays.stream(e.split(","))).distinct().collect(Collectors.toList());
        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
        roleInfoQueryWrapper.in("id",roleIds);
        List<RoleInfo> roleInfos = roleInfoMapper.selectList(roleInfoQueryWrapper);
        //组装
        List<UserListVo> userListVos = new ArrayList<>();
        records.forEach(e ->{
            UserListVo userListVo = new UserListVo();
            userListVo.setId(e.getId());
            userListVo.setUserName(e.getUserName());
            userListVo.setPassword(e.getPassword());
            List<String> list = Arrays.asList(e.getRoleList().split(","));
            List<RoleInfo> collect = roleInfos.stream().filter(f -> list.contains(f.getId().toString())).collect(Collectors.toList());
            userListVo.setRoleInfoList(collect);
            userListVos.add(userListVo);
        });
        return DataResult.createBySuccess(userListVos,myPage);
    }

    @Override
    public DataResult getRoleList(Integer pageSize, Integer pageNo,String search) {
        Page<RoleInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(search)){//增加搜索条件
            roleInfoQueryWrapper.like("role_name",search);
        }
        Page<RoleInfo> roleInfoPage = roleInfoMapper.selectPage(page, roleInfoQueryWrapper);

        List<RoleInfo> records = roleInfoPage.getRecords();
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(roleInfoPage.getTotal()+""));
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(new ArrayList<>(),myPage);
        }

        List<String> authorizationIds = records.stream().map(m -> m.getAuthorizationList()).flatMap(e -> Arrays.stream(e.split(","))).distinct().collect(Collectors.toList());
        QueryWrapper<AuthorizationInfo> authorizationInfoQueryWrapper = new QueryWrapper<>();
        authorizationInfoQueryWrapper.in("id",authorizationIds);
        List<AuthorizationInfo> authorizationInfos = authorizationInfoMapper.selectList(authorizationInfoQueryWrapper);
        ArrayList<RoleListVo> roleListVos = new ArrayList<>();
        records.forEach(e ->{
            RoleListVo roleListVo = new RoleListVo();
            roleListVo.setId(e.getId());
            roleListVo.setRoleName(e.getRoleName());
            List<String> list = Arrays.asList(e.getAuthorizationList().split(","));
            List<AuthorizationInfo> collect = authorizationInfos.stream().filter(f -> list.contains(f.getId().toString())).collect(Collectors.toList());
            roleListVo.setAuthorizationInfoList(collect);
            roleListVos.add(roleListVo);
        });
        return DataResult.createBySuccess(roleListVos,myPage);
    }

    @Override
    public DataResult getAuthorizationList(Integer pageSize, Integer pageNo,String search) {
        Page<AuthorizationInfo> page = new Page<>(pageNo,pageSize);
        QueryWrapper<AuthorizationInfo> authorizationInfoQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(search)){//增加搜索条件
            authorizationInfoQueryWrapper.like("authorization_name",search);
        }
        Page<AuthorizationInfo> authorizationInfoIPage = authorizationInfoMapper.selectPage(page, authorizationInfoQueryWrapper);
        List<AuthorizationInfo> records = authorizationInfoIPage.getRecords();
        MyPage myPage = new MyPage(pageNo, pageSize, Integer.valueOf(authorizationInfoIPage.getTotal()+""));
        if(CollectionUtils.isEmpty(records)){
            return DataResult.createBySuccess(new ArrayList<>(),myPage);
        }
        return DataResult.createBySuccess(records,myPage);
    }

    @Override
    public DataResult addUser(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult addRole(RoleInfo roleInfo) {
        roleInfoMapper.insert(roleInfo);
        //插入缓存
        redisTemplate.opsForValue().set(roleInfo.getRoleName(),roleInfo.getAuthorizationList());
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult addAuthorization(AuthorizationInfo authorizationInfo) {
        authorizationInfoMapper.insert(authorizationInfo);
        //插入缓存
        redisTemplate.opsForValue().set("authorization_"+authorizationInfo.getId(),authorizationInfo.getAuthorizationUrl());
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult deleteUser(Integer id) {
        userInfoMapper.deleteById(id);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult deleteRole(Integer id,String roleName) {
        roleInfoMapper.deleteById(id);
        //清除缓存
        redisTemplate.delete(roleName);
        return DataResult.createBySuccess();
    }

    @Override
    public DataResult deleteAuthorization(Integer id) {
        authorizationInfoMapper.deleteById(id);
        //清除缓存
        redisTemplate.delete("authorization_"+id);
        return DataResult.createBySuccess();
    }
}
