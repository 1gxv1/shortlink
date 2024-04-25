package com.chr1s.shortlink.admin.common.enums;

import com.chr1s.shortlink.admin.common.convention.errorCode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    UserNull("B000200", "用户记录不存在");
    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
    }
