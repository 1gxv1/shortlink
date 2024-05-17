package com.chr1s.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.project.common.convention.result.Result;
import com.chr1s.shortlink.project.common.convention.result.Results;
import com.chr1s.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkGroupCountReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        shortLinkService.createShortLink(requestParam);
        return Results.success(null);
    }

    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }

    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountRespDTO>> listGroupLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
