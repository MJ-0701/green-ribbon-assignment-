package com.example.greenribboncalimassignment.domain.hospital.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "hospitals")
@Comment("병원 데이터")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    @Comment("병원 PK")
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("병원 이름")
    private String name;
}
