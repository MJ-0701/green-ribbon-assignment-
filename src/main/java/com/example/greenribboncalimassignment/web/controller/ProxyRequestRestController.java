package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.response.ApiResponse;
import com.example.greenribboncalimassignment.service.proxy.ProxyRequestService;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.response.ProxyCreateResponse;
import com.example.greenribboncalimassignment.web.dto.response.ProxyDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "유저별 청구 대행 목록 조회", description = "특정 유저가 신청한 모든 청구 대행 목록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProxyDetailResponse.ProxyInfoDto>>> getProxyRequestList(
            @Parameter(description = "조회할 유저 ID", example = "1", required = true)
            @RequestParam Long userId
    ) {
        List<ProxyDetailResponse.ProxyInfoDto> response = proxyRequestService.getProxyRequestList(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "3.2 청구 대행 상세 조회", description = "특정 청구 대행 신청의 상세 정보(신청 정보, 병원 내역, 이력)를 조회합니다.")
    @GetMapping("/{proxyRequestId}")
    public ResponseEntity<ApiResponse<ProxyDetailResponse>> getProxyRequestDetail(
            @Parameter(description = "청구 대행 ID (PK)", example = "1", required = true)
            @PathVariable Long proxyRequestId
    ) {
        ProxyDetailResponse response = proxyRequestService.getProxyRequestDetail(proxyRequestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


}
