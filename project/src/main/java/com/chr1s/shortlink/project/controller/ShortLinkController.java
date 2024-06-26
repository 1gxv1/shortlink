package com.chr1s.shortlink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.project.common.constant.RuleConstant;
import com.chr1s.shortlink.project.common.constant.SentinelConstant;
import com.chr1s.shortlink.project.common.convention.result.Result;
import com.chr1s.shortlink.project.common.convention.result.Results;
import com.chr1s.shortlink.project.dto.req.*;
import com.chr1s.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.project.handler.CustomBlockHandler;
import com.chr1s.shortlink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = SentinelConstant.ruleName,
            blockHandler = SentinelConstant.ruleFuncName,
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success(null);
    }

    @GetMapping("{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        shortLinkService.restoreUrl(shortUri, request, response);
    }


    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }

    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountRespDTO>> listGroupLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }

}
