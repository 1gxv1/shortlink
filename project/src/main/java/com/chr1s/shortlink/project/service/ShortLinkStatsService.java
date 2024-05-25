package com.chr1s.shortlink.project.service;

import com.chr1s.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService{
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}
