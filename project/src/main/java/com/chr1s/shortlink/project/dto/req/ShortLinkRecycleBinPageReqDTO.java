package com.chr1s.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chr1s.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {
    private List<String> gidList;
}
