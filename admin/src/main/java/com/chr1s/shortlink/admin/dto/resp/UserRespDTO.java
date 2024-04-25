package com.chr1s.shortlink.admin.dto.resp;

import lombok.Data;

import java.util.Date;

@Data
public class UserRespDTO {


    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;


    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;



}
