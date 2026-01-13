package com.example.greenribboncalimassignment.web.dto.response;

import com.example.greenribboncalimassignment.common.CodeValue;

public record CodeResponse(
        String code,
        String description
) {
    public static CodeResponse from(CodeValue codeValue) {
        return new CodeResponse(codeValue.getCode(), codeValue.getDescription());
    }
}
