package com.example.greenribboncalimassignment.domain.user.entity;

import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "user_treatment")
@Comment("유저 진료 기록 (이력 데이터)")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    @Comment("진료 기록 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("진료 받은 유저")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    @Comment("방문한 병원 (FK)")
    private Hospital hospital;

    @Column(name = "hospital_name", nullable = false)
    @Comment("병원 이름 (삭제/변경 대비 스냅샷)")
    private String hospitalName;

    @Column(nullable = false)
    @Comment("진료 일자")
    private LocalDate treatmentDate;

    @Column(nullable = false)
    @Comment("진료비 금액")
    private Long amount;
}