package com.chr1s.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import com.chr1s.shortlink.project.dto.req.RecycleBinSaveReqDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {

    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
}
