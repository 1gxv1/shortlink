package com.chr1s.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

public interface ShortLinkRemoteService {

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
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
}
