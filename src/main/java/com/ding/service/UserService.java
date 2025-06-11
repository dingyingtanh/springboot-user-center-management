package com.ding.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ding.model.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author yuli
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-28 17:12:10
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 密码校验
     * @param planetCode 星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser 原始用户
     * @return 脱敏后的用户
     */
    User getSafetyUser(User originUser);


    /**
     * 用户注销
     * @param request 请求
     */
    int userLogout(HttpServletRequest request);
}
