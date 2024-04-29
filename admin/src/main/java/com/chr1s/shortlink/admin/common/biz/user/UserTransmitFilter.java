package com.chr1s.shortlink.admin.common.biz.user;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.chr1s.shortlink.admin.dao.entity.UserDo;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Objects;

import static com.chr1s.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String url=httpServletRequest.getRequestURI();
        if (!Objects.equals(url,"/api/short-link/v1/user/login/")){
            String token = httpServletRequest.getHeader("token");
            Object userInfoJsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + token,"user");
            if (userInfoJsonStr != null) {
                UserDo userDo = JSON.parseObject(userInfoJsonStr.toString(), UserDo.class);
                UserContext.setUser(BeanUtil.toBean(userDo, UserInfoDTO.class));
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }

    }
}
