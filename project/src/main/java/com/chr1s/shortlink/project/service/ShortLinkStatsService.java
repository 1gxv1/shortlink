package com.chr1s.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chr1s.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService{
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);
}
