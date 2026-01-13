package com.example.greenribboncalimassignment.domain.proxy.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.greenribboncalimassignment.config.JpaConfig;
import com.example.greenribboncalimassignment.config.QueryDslConfig;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.*;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
class ProxyRequestUnitRepositoryTest {

    @Autowired
    private ProxyRequestUnitRepository proxyRequestUnitRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("중복 신청 검증: 이미 종결(COMPLETED/DISCLAIMER)된 병원이라면 True를 반환한다.")
    void exists_processed_hospital_true() {
        // given
        Users user = Users.of("채명정");
        em.persist(user);

        Hospital hospitalA = Hospital.of("A병원");
        em.persist(hospitalA);

        // 과거 진료 기록 (이미 처리됨)
        UserTreatment oldTreatment = UserTreatment.of(user, hospitalA, LocalDate.now().minusDays(10), 1000L);
        em.persist(oldTreatment);

        // 1. 완료된 신청서 생성 (A병원 포함)
        ProxyRequest completedRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        ProxyRequestUnit unit = ProxyRequestUnit.builder()
                .userTreatment(oldTreatment)
                .missedAmount(1000L)
                .build();
        completedRequest.addUnit(unit);

        // 상태 강제 변경 (COMPLETED)
        completedRequest.updateStatus(ProxyStatus.IN_PROGRESS);
        completedRequest.updateStatus(ProxyStatus.FEE_CLAIM);
        completedRequest.updateStatus(ProxyStatus.COMPLETED);
        em.persist(completedRequest);

        em.flush();
        em.clear();

        // when
        boolean exists = proxyRequestUnitRepository.existsProcessedHospital(
                user.getId(),
                List.of(hospitalA.getId())
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("중복 신청 검증: 취소(CANCELLED)된 병원이라면 재신청이 가능하므로 False를 반환한다.")
    void exists_processed_hospital_false_when_cancelled() {
        // given
        Users user = Users.of("채명정");
        em.persist(user);
        Hospital hospitalB = Hospital.of("B병원");
        em.persist(hospitalB);

        UserTreatment treatment = UserTreatment.of(user, hospitalB, LocalDate.now(), 2000L);
        em.persist(treatment);

        // 취소된 신청서 생성
        ProxyRequest cancelledRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        cancelledRequest.addUnit(ProxyRequestUnit.builder().userTreatment(treatment).missedAmount(2000L).build());

        // 상태 변경 (CANCELLED)
        cancelledRequest.updateStatus(ProxyStatus.CANCELLED);
        em.persist(cancelledRequest);
        em.flush();

        // when
        boolean exists = proxyRequestUnitRepository.existsProcessedHospital(
                user.getId(),
                List.of(hospitalB.getId())
        );

        // then
        assertThat(exists).isFalse();
    }
}