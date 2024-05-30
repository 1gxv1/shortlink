package com.chr1s.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.ShortLinkBaseInfoRespDTO;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import com.chr1s.shortlink.admin.toolkit.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShortLinkController {
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success(null);
    }

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        return shortLinkRemoteService.pageShortLink(shortLinkPageReqDTO);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }




}
