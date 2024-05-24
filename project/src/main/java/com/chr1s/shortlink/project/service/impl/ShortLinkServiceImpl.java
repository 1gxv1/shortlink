package com.chr1s.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.project.common.constant.RedisKeyConstant;
import com.chr1s.shortlink.project.common.convention.exception.ServiceException;
import com.chr1s.shortlink.project.common.enums.ValidDateTypeEnum;
import com.chr1s.shortlink.project.dao.entity.LinkAccessStatsDTO;
import com.chr1s.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import com.chr1s.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.chr1s.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.chr1s.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.chr1s.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.chr1s.shortlink.project.dao.mapper.ShortLinkMapper;
import com.chr1s.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.project.service.ShortLinkService;
import com.chr1s.shortlink.project.toolkit.HashUtil;
import com.chr1s.shortlink.project.toolkit.LinkUtil;
import com.google.gson.JsonObject;
import groovy.util.logging.Slf4j;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.chr1s.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.chr1s.shortlink.project.common.constant.ShortLinkConstant.GAODE_URL;
import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        baseMapper.insert(shortLinkDO);

        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .gid(shortLinkDO.getGid())
                .build();
        shortLinkGotoMapper.insert(linkGotoDO);

        stringRedisTemplate.opsForValue().set(format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS);

        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }


    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(shortLinkPageReqDTO, eq);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));

    }

    @Override
    public List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid,count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(maps, ShortLinkGroupCountRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);

        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        if (shortLinkDO == null) throw new ClientException("服务器未找到对应的短链接！");

        ShortLinkDO build = ShortLinkDO.builder()
                .domain(shortLinkDO.getDomain())
                .shortUri(shortLinkDO.getShortUri())
                .clickNum(shortLinkDO.getClickNum())
                .favicon(shortLinkDO.getFavicon())
                .createdType(shortLinkDO.getCreatedType())
                .gid(shortLinkDO.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .build();

        if (Objects.equals(shortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT), ShortLinkDO::getValidDateType, null);
            baseMapper.update(build, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, shortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            shortLinkDO.setGid(requestParam.getGid());
            baseMapper.insert(shortLinkDO);
        }


    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originUrl = stringRedisTemplate.opsForValue().get(format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        if (StrUtil.isNotBlank(originUrl)) {
            shortLinkStats(fullShortUrl, null, request, response);
            ((HttpServletResponse) response).sendRedirect(originUrl);
            return;
        }
        RLock lock = redissonClient.getLock(format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originUrl = stringRedisTemplate.opsForValue().get(format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originUrl)) {
                ((HttpServletResponse) response).sendRedirect(originUrl);
                return;
            }

            LambdaQueryWrapper<ShortLinkGotoDO> eq = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(eq);
            if (shortLinkGotoDO == null) {
//            风控处理
                stringRedisTemplate.opsForValue().set(format(GOTO_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, shortLinkGotoDO.getFullShortUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(wrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(format(GOTO_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());

        } finally {
            lock.unlock();
        }

    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {

        if (StrUtil.isBlank(gid)) {
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
            gid = shortLinkGotoDO.getGid();
        }

        addShortLinkUV(fullShortUrl, gid, request, response);

        addShortLnkLocale(fullShortUrl, gid, request);

    }

    private void addShortLnkLocale(String fullShortUrl, String gid, ServletRequest request) {
        String ip = request.getRemoteAddr();
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", statsLocaleAmapKey);
        map.put("ip", ip);

        String result = HttpUtil.get(GAODE_URL, map);

        JSONObject localeResultObj = JSON.parseObject(result);

        String infoCode = localeResultObj.getString("infocode");

        if (StrUtil.isNotBlank(infoCode) && Objects.equals(infoCode, "10000")) {
            String province = localeResultObj.getString("province");
            boolean unknownFlag = province.equals("[]");
            LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .city(unknownFlag ? "未知" : localeResultObj.getString("city"))
                    .province(unknownFlag ? "未知" : localeResultObj.getString("province"))
                    .country(unknownFlag ? "未知" : "中国")
                    .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                    .cnt(1)
                    .date(new Date())
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);
        }
    }

    private void addShortLinkUV(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();

        Runnable addResponseTask = () -> {
            String uv = UUID.randomUUID().toString();
            Cookie uvCookie = new Cookie("uv", uv);
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            stringRedisTemplate.opsForSet().add(format(COOKIE_STATS_SHORT_LINK_KEY, fullShortUrl), uv);
            uvFirstFlag.set(true);
            ((HttpServletResponse) response).addCookie(uvCookie);
        };


        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        Long uvAdded = stringRedisTemplate.opsForSet().add(format(COOKIE_STATS_SHORT_LINK_KEY, fullShortUrl), each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseTask);
        } else {
            addResponseTask.run();
        }
        String remoteAddr = request.getRemoteAddr();
        Long uipAdded = stringRedisTemplate.opsForSet().add(format(IP_STATS_SHORT_LINK_KEY, fullShortUrl), remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;

        int hour = DateUtil.hour(new Date(), true);
        Week week = DateUtil.dayOfWeekEnum(new Date());
        int weekValue = week.getIso8601Value();

        LinkAccessStatsDTO linkAccessStatsDTO = LinkAccessStatsDTO.builder()
                .pv(1)
                .uv(uvFirstFlag.get() ? 1 : 0)
                .uip(uipFirstFlag ? 1 : 0)
                .hour(hour)
                .weekday(weekValue)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(new Date())
                .build();
        linkAccessStatsMapper.shortLinkStats(linkAccessStatsDTO);
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) throw new ServiceException("短链接生成频繁，请稍后再试！");
            String originUrl = requestParam.getOriginUrl();
            originUrl += UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) return faviconLink.attr("abs:href");
        }
        return null;
    }
}
