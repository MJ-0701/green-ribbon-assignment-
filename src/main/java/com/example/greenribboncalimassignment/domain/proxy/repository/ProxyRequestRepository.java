package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRequestRepository extends JpaRepository<ProxyRequest, Long>, ProxyRequestRepositoryCustom {
}
