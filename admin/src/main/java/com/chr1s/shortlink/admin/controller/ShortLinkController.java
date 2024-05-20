package com.chr1s.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

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

}
