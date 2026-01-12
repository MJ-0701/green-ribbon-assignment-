package com.example.greenribboncalimassignment.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "users")
@Comment("사용자 정보")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Comment("사용자 PK")
    private Long id;

    @Column(nullable = false, length = 50)
    @Comment("사용자 이름")
    private String name;

    // --- 정적 팩토리 메서드 ---
    public static Users of(String name) {
        return Users.builder()
                .name(name)
                .build();
    }
}
