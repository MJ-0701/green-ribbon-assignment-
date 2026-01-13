package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.response.ApiResponse;
import com.example.greenribboncalimassignment.service.proxy.ProxyRequestService;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.response.ProxyCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01. 청구 대행 (Proxy)", description = "청구 대행 신청 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/proxy-requests")
public class ProxyRequestRestController {

    private final ProxyRequestService proxyRequestService;

    @Operation(summary = "3.1 청구 대행 신청", description = "유저가 선택한 병원 진료 기록들에 대해 청구 대행을 신청합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ProxyCreateResponse>> createProxyRequest(
            @Valid @RequestBody ProxyCreateRequest request
    ) {
        ProxyCreateResponse response = proxyRequestService.createProxyRequest(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
