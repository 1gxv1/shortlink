package com.chr1s.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO recycleBinSaveReqDTO) {
        recycleBinService.saveRecycleBin(recycleBinSaveReqDTO);
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public com.chr1s.shortlink.project.common.convention.result.Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO shortLinkPageReqDTO) {
        return com.chr1s.shortlink.project.common.convention.result.Results.success(recycleBinService.pageShortLink(shortLinkPageReqDTO));
    }

}
