package com.chr1s.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import com.chr1s.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.chr1s.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {

    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

   void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}
