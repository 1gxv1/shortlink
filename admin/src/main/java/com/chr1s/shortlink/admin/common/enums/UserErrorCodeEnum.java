package com.chr1s.shortlink.admin.common.enums;

import com.chr1s.shortlink.admin.common.convention.errorCode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_TOKEN_NULL("A000200", "用户token为空！"),

    USER_TOKEN_FAIL("A000201", "用户token错误！"),

    UserNull("B000200", "用户记录不存在"),

    USER_EXIST("B000202", "用户名重复"),

    USER_SAVE_ERROR("B000203", "用户插入失败"),

    USER_UPDATE_ERROR("B000204", "用户更新信息失败");


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
