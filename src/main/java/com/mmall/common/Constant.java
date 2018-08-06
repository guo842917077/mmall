package com.mmall.common;

/**
 * Created by apple on 2018/8/1.
 */
public class Constant {
    public static final String CURRENT_USER = "CURRENT_USER";
    public static final String EMAIL = "EMAIL";
    public static final String USERNAME = "USERNAME";

    public interface Role{
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1;//管理员
    }
}
