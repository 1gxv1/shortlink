package com.chr1s.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.admin.common.biz.user.UserInfoDTO;
import com.chr1s.shortlink.admin.common.constant.RedisCacheConstant;
import com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.admin.dao.entity.UserDo;
import com.chr1s.shortlink.admin.dao.mapper.UserMapper;
import com.chr1s.shortlink.admin.dto.req.UserLoginReqDTO;
import com.chr1s.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.chr1s.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.chr1s.shortlink.admin.dto.resp.UserRespDTO;
import com.chr1s.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.chr1s.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;
import static com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum.USER_UPDATE_ERROR;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

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
    public void register(UserRegisterReqDTO requestParam) {
        if (isExistUser(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        }
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_Register_KEY + requestParam.getUsername());
        try {
            if (lock.tryLock()) {
                int res = baseMapper.insert(BeanUtil.toBean(requestParam, UserDo.class));
                if (res < 1) {
                    throw new ClientException(USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
//        TODO: 验证当前登录用户是登录用户
        LambdaUpdateWrapper<UserDo> lambdaUpdateWrapper = Wrappers.lambdaUpdate(UserDo.class)
                .eq(UserDo::getUsername, requestParam.getUsername());
        int update = baseMapper.update(BeanUtil.toBean(requestParam, UserDo.class), lambdaUpdateWrapper);
        if (update < 1) throw new ClientException(USER_UPDATE_ERROR);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDo> eq = Wrappers.lambdaQuery(UserDo.class)
                .eq(UserDo::getUsername, requestParam.getUsername())
                .eq(UserDo::getPassword, requestParam.getPassword());
        UserDo userDo = baseMapper.selectOne(eq);
        if (userDo == null) throw new ClientException("数据库访问失败，可能是用户名或者密码错误");
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        String key = USER_LOGIN_KEY + s;
        stringRedisTemplate.opsForHash().put(key, "user",JSON.toJSONString(userDo));
        return new UserLoginRespDTO(s);
    }
}
