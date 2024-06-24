package com.chr1s.shortlink.admin.common.biz.user;

import ch.qos.logback.core.net.server.Client;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dao.entity.UserDo;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.chr1s.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;
import static com.chr1s.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_NULL;

@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/actual/user/exist",
            "/api/short-link/admin/v1/user/register",
            "/api/short-link/admin/v1/title"
    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token = httpServletRequest.getHeader("token");
        if (StrUtil.isNotBlank(token)) {
            String userId = httpServletRequest.getHeader("userId");
            String realName = httpServletRequest.getHeader("realName");
            String username = httpServletRequest.getHeader("username");
            UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception ignored) {
            throw new ClientException(ignored.toString());
        } finally {
            UserContext.removeUser();
        }
    }

}
