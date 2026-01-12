package com.example.greenribboncalimassignment.domain.user.entity;

import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "user_treatment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserTreatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "hospital_name", nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private LocalDate treatmentDate;

    @Column(nullable = false)
    private Long amount;

}