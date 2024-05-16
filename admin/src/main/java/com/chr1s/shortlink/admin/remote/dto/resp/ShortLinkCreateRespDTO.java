package com.chr1s.shortlink.admin.remote.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkCreateRespDTO {

    private String gid;

    /**
     * 原始链接
     */
    private String originUrl;


    private String fullShortUrl;

}
