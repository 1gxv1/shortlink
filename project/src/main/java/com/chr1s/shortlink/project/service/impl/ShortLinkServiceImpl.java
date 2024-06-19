package com.chr1s.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.chr1s.shortlink.project.dao.entity.*;
import com.chr1s.shortlink.project.dao.mapper.*;
import com.chr1s.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.project.dto.resp.*;
import com.chr1s.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.chr1s.shortlink.project.service.LinkStatsTodayService;
import com.chr1s.shortlink.project.service.ShortLinkService;
import com.chr1s.shortlink.project.toolkit.HashUtil;
import com.chr1s.shortlink.project.toolkit.LinkUtil;
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
import org.redisson.api.RReadWriteLock;
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
import java.util.concurrent.atomic.AtomicReference;

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

    private final LinkOStatsMapper linkOStatsMapper;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkStatsTodayService linkStatsTodayService;

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        //判断短链接创建的时候的时间是不是在这之前，是的话直接抛异常
        if (requestParam.getValidDateType() == 1 && requestParam.getValidDate().before(new Date())) {
            throw new ClientException("短链接设置的有效期时间比当前时间早,短链接无效");
        }
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = createShortLinkDefaultDomain + "/" + shortLinkSuffix;

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
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
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(shortLinkPageReqDTO);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid,count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(maps, ShortLinkGroupCountRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);

        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) throw new ClientException("服务器未找到对应的短链接！");


        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
//            当判断出gid是相同的时候，说明修改的不是gid，可以正常地执行相关的修改
            ShortLinkDO newShortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .clickNum(hasShortLinkDO.getClickNum())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(hasShortLinkDO.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDate(requestParam.getValidDate())
                    .validDateType(requestParam.getValidDateType())
                    .build();

            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT), ShortLinkDO::getValidDateType, null);
            baseMapper.update(newShortLinkDO, updateWrapper);
        } else {
//            发生了gid的修改，此时需要引入读写锁去上锁再修改数据库
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            if (!rLock.tryLock()) throw new ServiceException("短链接正在被访问，请稍后再试！");
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);

                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
//将前面一个gid的短链接直接禁用，用delTime和fullShortUrl来唯一标识一个短链接，再重新根据gid创造一个新的短链接入库
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);

                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);

                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);

                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList());
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
//修改短链接goto表
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);

                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } catch (Exception e) {
                throw new ClientException("短链接修改gid的时候发生错误");
            } finally {
                rLock.unlock();
            }
        }
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType()) || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
        }
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String serverPort = String.valueOf(request.getServerPort());
        if (serverPort.equals("80")) {
            serverPort = "";
        } else serverPort = ":" + serverPort;
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originUrl = stringRedisTemplate.opsForValue().get(format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        if (StrUtil.isNotBlank(originUrl)) {
            if (originUrl.equals("-")) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            ((HttpServletResponse) response).sendRedirect(originUrl);
            return;
        }
        RLock lock = redissonClient.getLock(format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
//            双重检测锁进行缓存重建，在找不到短链接的时候会到这里来，但上锁需要时间，可能别人已经进行了缓存重建，因此需要再判定一次存不存在，然后进行缓存重建，避免击穿
            originUrl = stringRedisTemplate.opsForValue().get(format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originUrl)) {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                ((HttpServletResponse) response).sendRedirect(originUrl);

                return;
            }

            LambdaQueryWrapper<ShortLinkGotoDO> eq = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(eq);
            if (shortLinkGotoDO == null) {
//           风控处理
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
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), statsRecord);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());

        } finally {
            lock.unlock();
        }

    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
//            uv为辨识访问的用户所生成的随机字符串
            uv.set(UUID.randomUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：" + originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }




    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {

        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
//上读锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
//            上锁失败，说明有人正在修改短链接，需要把记录放进mq当中
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
//        此时拿到锁，能够正常的访问
        try {
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
//            插入第一个表shortLinkStats
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();

            LinkAccessStatsDO linkAccessStatsDTO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDTO);


            HashMap<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", statsRecord.getRemoteAddr());
            String result = HttpUtil.get(GAODE_URL, localeParamMap);

            addShortLnkLocale(fullShortUrl, gid, result);

            addShortLinkOs(fullShortUrl, gid, statsRecord.getOs());

            addShortLinkBrowser(fullShortUrl, gid, statsRecord.getBrowser());

            addShortLinkFrequency(fullShortUrl, gid, statsRecord, result);

            addShortLinkDevice(fullShortUrl, gid, statsRecord.getDevice());

            addShortLinkNetwork(fullShortUrl, gid, statsRecord.getNetwork());

        } catch (Exception ex) {
            log.error("短链接访问量统计异常", ex);
        }finally {
            rLock.unlock();
        }
    }

    private void addShortLinkNetwork(String fullShortUrl, String gid, String network) {
        LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                .network(network)
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(new Date())
                .cnt(1)
                .build();
        linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

    }

    private void addShortLinkDevice(String fullShortUrl, String gid, String device) {
        LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                .device(device)
                        .gid(gid)
                        .date(new Date())
                        .fullShortUrl(fullShortUrl)
                        .cnt(1)
                        .build();

        linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

    }

    private void addShortLinkFrequency(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord, String res) {
        JSONObject resObject = JSON.parseObject(res);
        String actualProvince = Objects.equals(resObject.getString("province"), "[]") ? "未知" : resObject.getString("province");
        String actualCity = Objects.equals(resObject.getString("city"), "[]") ? "未知" : resObject.getString("city");

        LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                .user(statsRecord.getUv())
                .ip(statsRecord.getRemoteAddr())
                .os(statsRecord.getOs())
                .browser(statsRecord.getBrowser())
                .gid(gid)
                .network(statsRecord.getNetwork())
                .device(statsRecord.getDevice())
                .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                .fullShortUrl(fullShortUrl)
                .build();
        linkAccessLogsMapper.insert(linkAccessLogsDO);
    }

    private void addShortLinkBrowser(String fullShortUrl, String gid, String browser) {
        LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                .browser(browser)
                .fullShortUrl(fullShortUrl)
                .date(new Date())
                .gid(gid)
                .cnt(1)
                .build();
        linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);
    }

    private void addShortLinkOs(String fullShortUrl, String gid, String os) {
        LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                .os(os)
                .fullShortUrl(fullShortUrl)
                .date(new Date())
                .gid(gid)
                .cnt(1)
                .build();
        linkOStatsMapper.shortLinkOsStats(linkOsStatsDO);
    }

    private void addShortLnkLocale(String fullShortUrl, String gid, String result) {

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
