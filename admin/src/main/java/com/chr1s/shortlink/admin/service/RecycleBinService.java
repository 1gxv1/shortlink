package com.chr1s.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService {

    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}
