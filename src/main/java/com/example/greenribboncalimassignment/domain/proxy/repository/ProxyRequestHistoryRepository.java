package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyRequestHistoryRepository extends JpaRepository<ProxyRequestHistory, Long> {
}