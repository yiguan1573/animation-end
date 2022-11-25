package com.yiguan.common;

/**
 * @Author: lw
 * @CreateTime: 2022-10-06  16:01
 * @Description: TODO
 * @Version: 1.0
 */
public class Constant {
    //未授权
    public static int UNAUTHORIZED = 401;
    //禁止
    public static int FORBIDDEN = 403;
    //未找到
    public static int NOT_FONUD = 404;
    //请求超时
    public static int TIME_OUT = 408;
    //授权服务器check_token地址
    public static String CHECK_TOKEN_URL="http://animation-auth/oauth/check_token";
    //授权服务器请求token地址
    public static String TOKEN_URL="http://animation-auth/oauth/token";
    //根据用户名刷新token
    public static String REFRESH_TOKEN_URL="http://animation-auth/token/refreshToken";
    //退出
    public static String LOGOUT_URL="http://animation-auth/logout";
    //刷新缓存
    public static String REFRESH_CASH_URL="http://animation-auth/token/refreshCash";
}
