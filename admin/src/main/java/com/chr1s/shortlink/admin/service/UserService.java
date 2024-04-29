package com.chr1s.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chr1s.shortlink.admin.dao.entity.UserDo;
import com.chr1s.shortlink.admin.dto.req.UserLoginReqDTO;
import com.chr1s.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chr1s.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.chr1s.shortlink.admin.dto.resp.UserRespDTO;
import org.springframework.stereotype.Service;


public interface UserService extends IService<UserDo> {
    UserRespDTO getUserByUserName(String username);

    Boolean isExistUser(String username);


    void register(UserRegisterReqDTO requestParam);

    void update(UserUpdateReqDTO requestParam);

    UserLoginRespDTO login(UserLoginReqDTO requestParam);


}
