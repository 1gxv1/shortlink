package com.chr1s.shortlink.admin.remote.dto.req;

import lombok.Data;


@Data
public class ShortLinkStatsReqDTO {
    private String fullShortUrl;

    private String gid;

    private String startDate;

    private String endDate;
}
