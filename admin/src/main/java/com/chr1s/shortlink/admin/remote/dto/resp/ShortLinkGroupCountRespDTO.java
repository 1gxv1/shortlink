package com.chr1s.shortlink.admin.remote.dto.resp;

import lombok.Data;

@Data
public class ShortLinkGroupCountRespDTO {
    private String gid;
    private Integer shortLinkCount;
}
