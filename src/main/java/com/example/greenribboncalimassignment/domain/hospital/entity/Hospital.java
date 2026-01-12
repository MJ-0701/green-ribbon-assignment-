package com.example.greenribboncalimassignment.domain.hospital.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "hospitals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}
