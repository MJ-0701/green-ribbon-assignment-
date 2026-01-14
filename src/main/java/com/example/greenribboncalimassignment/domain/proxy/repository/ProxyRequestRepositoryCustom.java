package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.web.dto.response.ProxyDetailResponse;

import java.util.List;

public interface ProxyRequestRepositoryCustom {

    ProxyDetailResponse findProxyDetail(Long proxyRequestId);

    List<ProxyDetailResponse.ProxyInfoDto> findAllByUserId(Long userId);
}
