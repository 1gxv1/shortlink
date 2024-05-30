package com.chr1s.shortlink.admin.remote.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chr1s.shortlink.admin.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.*;
import com.chr1s.shortlink.admin.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkRemoteService {

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("orderTag", requestParam.getOrderTag());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", map);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    default Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        String res = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(res, new TypeReference<>() {
        });
    }

    default Result<List<ShortLinkGroupCountRespDTO>> listGroupLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("requestParam", requestParam);
        String res = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", map);
        return JSON.parseObject(res, new TypeReference<>() {
        });
    }

    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        String res = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
    }

    default Result<String> getTitleByUrl(@RequestParam("url") String url) {
        String title = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(title, new TypeReference<>() {
        });
    }

    default Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        String res = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recylce-bin/save", JSON.toJSONString(requestParam));
        return Results.success();
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gidList", requestParam.getGidList());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", map);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
    }

    default void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
    }

    default Result<ShortLinkStatsRespDTO> shortLinkAccessRecord(ShortLinkStatsReqDTO requestParam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fullShortUrl", requestParam.getFullShortUrl());
        map.put("gid", requestParam.getGid());
        map.put("startDate", requestParam.getStartDate());
        map.put("endDate", requestParam.getEndDate());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", map);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", stringObjectMap);

        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问分组短链接监控访问记录请求参数
     * @return 分组短链接监控访问记录信息
     */
    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问分组短链接监控访问记录请求参数
     * @return 分组短链接监控访问记录信息
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", stringObjectMap);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }
}
