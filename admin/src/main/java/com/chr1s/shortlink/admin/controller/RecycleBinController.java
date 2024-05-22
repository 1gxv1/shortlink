package com.chr1s.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @PostMapping("/api/short-link/v1/admin/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        return shortLinkRemoteService.saveRecycleBin(recycleBinSaveReqDTO);
    }

    @GetMapping("/api/short-link/v1/admin/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkPageReqDTO) {
        return recycleBinService.pageRecycleBinShortLink(shortLinkPageReqDTO);
    }
}
