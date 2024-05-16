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
            "/api/short-link/admin/v1/user/register"
    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String url = httpServletRequest.getRequestURI();
        if (!IGNORE_URI.contains(url)) {
            String token = httpServletRequest.getHeader("token");
            if (!StrUtil.isAllNotBlank(token)) {
                returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_NULL))));
                return;
            }
            Object userInfoJsonStr;
            try {
                userInfoJsonStr = stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + token, "user");
                if (userInfoJsonStr == null) throw new ClientException(USER_TOKEN_FAIL);
            } catch (Exception e) {
                returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FAIL))));
                return;
            }
            UserDo userDo = JSON.parseObject(userInfoJsonStr.toString(), UserDo.class);
            UserContext.setUser(BeanUtil.toBean(userDo, UserInfoDTO.class));

        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception ignored) {
            throw new ClientException(ignored.toString());
        } finally {
            UserContext.removeUser();
        }
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);
        } catch (IOException e) {

        } finally {
            if (writer != null) writer.close();
        }
    }
}
