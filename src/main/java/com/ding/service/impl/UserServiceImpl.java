package com.ding.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ding.common.ErrorCode;
import com.ding.exception.BusinessException;
import com.ding.model.User;
import com.ding.service.UserService;
import com.ding.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.ding.contant.UserConstant.USER_LOGIN_STATE;


/**
* @author yuli
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-28 17:12:10
*/
@Service
@Slf4j

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    /**
     * 加盐 混淆密码
     */
    private static final String SALT = "yuli";


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 密码校验
     * @param planetCode 星球编号
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,  "用户密码过短");
        }
        // 账号不能包含特殊字符（只允许字母、数字和下划线）
        if (!userAccount.matches("\\w{4,}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,  "星球编号过长");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long userCount = userMapper.selectCount(queryWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,  "账号重复");
        }
        //星球编号不能重复
        queryWrapper.eq("planetCode", planetCode);
        long planetCount = userMapper.selectCount(queryWrapper);
        if (planetCount > 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"星球编号重复");
        }
        //  2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账号不能包含特殊字符（只允许字母、数字和下划线）
        if (!userAccount.matches("\\w{4,}")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊字符");
        }
        //  2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        //用户脱敏
        User safeUser = getSafetyUser(user);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        //4. 返回脱敏后的用户信息
        return safeUser;
    }

    /**
     * 用户脱敏
     * @param originUser 原始用户
     * @return 脱敏后的用户
     */
    @Override
         public User getSafetyUser(@NotNull User originUser) {
         User safeUser = new User();
         safeUser.setId(originUser.getId());
         safeUser.setUsername(originUser.getUsername());
         safeUser.setUserAccount(originUser.getUserAccount());
         safeUser.setAvatarUrl(originUser.getAvatarUrl());
         safeUser.setUserRole(originUser.getUserRole());
         safeUser.setGender(originUser.getGender());
         safeUser.setPhone(originUser.getPhone());
         safeUser.setEmail(originUser.getEmail());
         safeUser.setUserStatus(originUser.getUserStatus());
         safeUser.setCreateTime(originUser.getCreateTime());
         safeUser.setPlanetCode(originUser.getPlanetCode());
         return safeUser;
    }

    /**
     * 用户注销
     * @param request 请求
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return 1;
    }

}