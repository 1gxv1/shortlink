package com.chr1s.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import com.chr1s.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO);

    List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParam);

    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException;

    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord);
}
