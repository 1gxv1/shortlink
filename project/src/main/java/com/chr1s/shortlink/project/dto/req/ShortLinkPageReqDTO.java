package com.chr1s.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {
    private String gid;
}

