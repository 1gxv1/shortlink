package com.chr1s.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.admin.dao.entity.UserDo;
import com.chr1s.shortlink.admin.dao.mapper.UserMapper;
import com.chr1s.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chr1s.shortlink.admin.dto.resp.UserRespDTO;
import com.chr1s.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Override
    public UserRespDTO getUserByUserName(String username) {
        LambdaQueryWrapper<UserDo> queryWrapper = Wrappers.lambdaQuery(UserDo.class).eq(UserDo::getUsername, username);
        UserDo userDo = baseMapper.selectOne(queryWrapper);
        UserRespDTO result = new UserRespDTO();
        if (userDo == null) {
            throw new ClientException(UserErrorCodeEnum.UserNull);
        }
        BeanUtils.copyProperties(userDo, result);
        return result;
    }

    @Override
    public Boolean isExistUser(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void Register(UserRegisterReqDTO requestParam) {
        if (isExistUser(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        }
        int res = baseMapper.insert(BeanUtil.toBean(requestParam, UserDo.class));
        if (res < 1) {
            throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
        }
        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
    }
}
