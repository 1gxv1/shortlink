package com.chr1s.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chr1s.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.chr1s.shortlink.admin.remote.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;


    @PostMapping("/api/short-link/v1/admin/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        return shortLinkActualRemoteService.saveRecycleBin(recycleBinSaveReqDTO);
    }

    @GetMapping("/api/short-link/v1/admin/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkPageReqDTO) {
        return recycleBinService.pageRecycleBinShortLink(shortLinkPageReqDTO);
    }

    @PostMapping("/api/short-link/v1/admin/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        shortLinkActualRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    @DeleteMapping("/api/short-link/v1/admin/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkActualRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
