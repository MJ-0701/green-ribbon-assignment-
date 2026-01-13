package com.example.greenribboncalimassignment.domain.user.entity;

import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserTreatmentTest {

    @DisplayName("팩토리 메서드: 진료 기록 생성 시 병원 이름이 스냅샷으로 저장된다.")
    @Test
    void create_snapshot_hospital_name() {
        // given
        Users user = Users.of("채명정");
        Hospital hospital = Hospital.of("연세세브란스"); // 현재 병원 이름

        // when
        UserTreatment treatment = UserTreatment.of(user, hospital, LocalDate.now(), 10000L);

        // then
        assertThat(treatment.getHospitalName()).isEqualTo("연세세브란스");

        // 검증: 병원 객체와 연결은 되어있지만, hospitalName 필드는 독립적인 값이어야 함
        assertThat(treatment.getHospital()).isEqualTo(hospital);
    }
}