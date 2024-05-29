package com.chr1s.shortlink.admin.remote.dto.req;

import cn.hutool.db.Page;
import lombok.Data;

@Data
public class ShortLinkGroupStatsReqDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
