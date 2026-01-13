package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.CodeValue;
import com.example.greenribboncalimassignment.common.response.ApiResponse;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.utils.EnumMapper;
import com.example.greenribboncalimassignment.web.dto.response.CodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "00. 공통 (Common)", description = "공통 코드 및 설정 조회 API")
@RestController
@RequestMapping("/api/common")
public class EnumRestController {

    @Operation(summary = "모든 공통 코드 조회", description = "시스템에서 사용하는 모든 Enum 코드 목록을 조회합니다.")
    @GetMapping("/codes")
    public ApiResponse<Map<String, List<CodeResponse>>> getAllCodes() {

        Map<String, Class<? extends CodeValue>> factory = new LinkedHashMap<>();
        factory.put("guaranteeTypes", GuaranteeType.class);
        factory.put("proxyStatuses", ProxyStatus.class);

        return ApiResponse.success(EnumMapper.toMap(factory));
    }
}