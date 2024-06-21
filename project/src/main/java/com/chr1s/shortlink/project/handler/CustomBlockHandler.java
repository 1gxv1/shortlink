package com.chr1s.shortlink.project.handler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chr1s.shortlink.project.common.convention.result.Result;
import com.chr1s.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod() {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问人数过多，请稍后再试！");
    }
}
