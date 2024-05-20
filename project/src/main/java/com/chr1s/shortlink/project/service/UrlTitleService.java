package com.chr1s.shortlink.project.service;

import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

public interface UrlTitleService {

    String getTitleByUrl(@RequestParam("url") String url) throws IOException;

}
