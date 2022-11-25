package com.yiguan.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yiguan.bean.SecurityUser;
import com.yiguan.bean.entity.RoleInfo;
import com.yiguan.bean.entity.UserInfo;
import com.yiguan.dao.RoleInfoMapper;
import com.yiguan.dao.UserInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: lw
 * @CreateTime: 2022-10-09  21:19
 * @Description: TODO:自定义登录逻辑
 * @Version: 1.0
 */
@Component
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //在库中查找是否有该用户
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",s);
        List<UserInfo> userInfos = userInfoMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(userInfos)){
            throw new UsernameNotFoundException("用户"+s+"不存在");
        }
        String roleList = "";
        if(StringUtils.isEmpty(userInfos.get(0).getRoleList())){
            //用户没有角色，默认为普通角色
            roleList = "ROLE_COMMON";
        }
        QueryWrapper<RoleInfo> roleInfoQueryWrapper = new QueryWrapper<>();
        roleInfoQueryWrapper.in("id", Arrays.asList(userInfos.get(0).getRoleList().split(",")));
        List<RoleInfo> roleInfos = roleInfoMapper.selectList(roleInfoQueryWrapper);
        List<String> strings = roleInfos.stream().map(m -> m.getRoleName()).collect(Collectors.toList());
        roleList = String.join(",",strings);
        return new SecurityUser(s,passwordEncoder.encode(userInfos.get(0).getPassword()), AuthorityUtils.commaSeparatedStringToAuthorityList(roleList));
    }
}
