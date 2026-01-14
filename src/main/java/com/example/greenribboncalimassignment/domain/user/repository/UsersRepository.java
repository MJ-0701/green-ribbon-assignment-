package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.domain.user.entity.Users;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Users u where u.id = :id")
    Optional<Users> findByIdWithLock(@Param("id") Long id);
}
