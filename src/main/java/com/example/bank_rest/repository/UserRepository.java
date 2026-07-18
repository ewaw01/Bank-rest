package com.example.bank_rest.repository;

import com.example.bank_rest.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = """
    SELECT * FROM users u
        WHERE (:id IS NULL OR u.id=:id)
        AND (:email IS NULL OR u.email=:email)
        AND (:username IS NULL OR u.username=:username)
    """, nativeQuery = true)
    Page<UserEntity> findAllByFilter(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("username") String username,
            Pageable pageable
    );
}
