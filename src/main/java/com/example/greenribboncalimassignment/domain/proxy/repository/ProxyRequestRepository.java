package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProxyRequestRepository extends JpaRepository<ProxyRequest, Long> {


    boolean existsByUserIdAndStatusIn(Long userId, List<ProxyStatus> statuses);

    default boolean existsOngoingRequest(Long userId) {
        return existsByUserIdAndStatusIn(userId, List.of(
                ProxyStatus.PENDING,
                ProxyStatus.IN_PROGRESS,
                ProxyStatus.FEE_CLAIM
        ));
    }
}
