package com.example.greenribboncalimassignment;

import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.hospital.repository.HospitalRepository;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Profile("!prod") // 운영 환경 제외, 로컬/테스트에서만 동작
public class DataInitializer implements ApplicationRunner {

    private final UsersRepository usersRepository;
    private final HospitalRepository hospitalRepository;
    private final UserTreatmentRepository userTreatmentRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usersRepository.count() > 0) return; // 데이터 있으면 스킵

        // 1. 병원 데이터 생성 (10개)
        List<Hospital> hospitals = new ArrayList<>();
        String[] hospitalNames = {
                "세브란스병원", "서울아산병원", "삼성서울병원", "서울대학교병원", "강남성모병원",
                "고대안암병원", "건국대학교병원", "이대목동병원", "아주대학교병원", "분당서울대병원"
        };

        for (String hospitalName : hospitalNames) {
            hospitals.add(Hospital.builder()
                    .name(hospitalName)
                    .build());
        }
        hospitalRepository.saveAll(hospitals);

        // 2. 유저 데이터 생성 (15명)
        List<Users> users = new ArrayList<>();
        String[] userNames = {
                "채명정", "김철수", "이영희", "박민수", "정수진",
                "강동원", "한지민", "송중기", "아이유", "박보검",
                "김유정", "차은우", "장원영", "카리나", "손흥민"
        };

        for (String name : userNames) {
            users.add(Users.builder()
                    .name(name)
                    .build());
        }
        usersRepository.saveAll(users);

        // 3. 진료 기록 데이터 생성 (30개 - 랜덤 매칭)
        List<UserTreatment> treatments = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            // 랜덤 유저 & 병원 선택
            Users randomUser = users.get(random.nextInt(users.size()));
            Hospital randomHospital = hospitals.get(random.nextInt(hospitals.size()));

            // 랜덤 날짜 (2022~2025년 사이)
            LocalDate randomDate = LocalDate.of(2022 + random.nextInt(4), 1 + random.nextInt(12), 1 + random.nextInt(28));

            // 랜덤 금액 (1만원 ~ 100만원 단위)
            long randomAmount = (random.nextInt(100) + 1) * 10000L;

            treatments.add(UserTreatment.builder()
                    .user(randomUser)
                    .hospital(randomHospital)
                    .hospitalName(randomHospital.getName()) // Snapshot 저장
                    .treatmentDate(randomDate)
                    .amount(randomAmount)
                    .build());
        }

        Users mj = users.get(0); // 채명정
        treatments.add(createFixedTreatment(mj, hospitals.get(0), "2023-05-01", 100_000L)); // 세브란스
        treatments.add(createFixedTreatment(mj, hospitals.get(1), "2023-06-15", 250_000L)); // 아산
        treatments.add(createFixedTreatment(mj, hospitals.get(0), "2023-07-20", 500_000L)); // 세브란스 (재방문)

        userTreatmentRepository.saveAll(treatments);

        System.out.println("=============== [DataInitializer] 테스트 데이터 세팅 완료 (User: 15, Hospital: 10, Treatment: 33) ===============");
    }

    private UserTreatment createFixedTreatment(Users user, Hospital hospital, String dateStr, Long amount) {
        return UserTreatment.builder()
                .user(user)
                .hospital(hospital)
                .hospitalName(hospital.getName())
                .treatmentDate(LocalDate.parse(dateStr))
                .amount(amount)
                .build();
    }
}
