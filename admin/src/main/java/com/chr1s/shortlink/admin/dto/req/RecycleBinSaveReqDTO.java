package com.chr1s.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class RecycleBinSaveReqDTO {

    private String gid;

    private String fullShortUrl;
}
