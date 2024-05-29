package com.chr1s.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chr1s.shortlink.project.dao.entity.LinkAccessLogsDO;
import lombok.Data;


@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {

    private String gid;

    private String startDate;

    private String endDate;
}
