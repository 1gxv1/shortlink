package com.chr1s.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;


@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page {
    private String fullShortUrl;

    private String gid;

    private String startDate;

    private String endDate;
}
