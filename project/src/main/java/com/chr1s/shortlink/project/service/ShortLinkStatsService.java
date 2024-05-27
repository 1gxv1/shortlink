package com.chr1s.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService{
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
}
