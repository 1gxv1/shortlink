package com.chr1s.shortlink.project.common.enums;

import lombok.*;

@RequiredArgsConstructor
@Getter
public enum ValidDateTypeEnum {
    PERMANENT(0),

    CUSTOM(1);
    private final int type;
}
