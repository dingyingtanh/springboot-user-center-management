package com.ding.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author dingy
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -3728967128911197552L;

    private String userAccount;

    private String userPassword;
}
