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
    @DisplayName("중복 신청 검증: 이미 COMPLETED 된 신청서에 포함된 진료기록 ID라면 True를 반환한다.")
    void exists_by_treatment_and_status() {
        // given
        Users user = Users.of("채명정");
        em.persist(user);

        Hospital hospital = Hospital.of("A병원");
        em.persist(hospital);

        UserTreatment treatment = UserTreatment.of(user, hospital, LocalDate.now(), 1000L);
        em.persist(treatment);

        // 완료된 신청서 생성
        ProxyRequest completedRequest = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        completedRequest.addUnit(ProxyRequestUnit.from(treatment)); // Unit 추가

        // 강제로 상태 변경
        completedRequest.updateStatus(ProxyStatus.IN_PROGRESS);
        completedRequest.updateStatus(ProxyStatus.FEE_CLAIM);
        completedRequest.updateStatus(ProxyStatus.COMPLETED);

        em.persist(completedRequest);
        em.flush();

        // when
        boolean exists = proxyRequestUnitRepository.existsByUserTreatmentIdInAndProxyRequest_StatusIn(
                List.of(treatment.getId()),
                List.of(ProxyStatus.COMPLETED, ProxyStatus.DISCLAIMER)
        );

        // then
        assertThat(exists).isTrue();
    }
}