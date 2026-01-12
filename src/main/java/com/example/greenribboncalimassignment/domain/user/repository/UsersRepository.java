package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
