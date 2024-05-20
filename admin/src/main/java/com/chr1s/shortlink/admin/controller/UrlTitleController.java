package com.chr1s.shortlink.admin.controller;

import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) throws IOException {
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
