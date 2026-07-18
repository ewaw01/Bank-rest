package com.example.bank_rest.repository;

import com.example.bank_rest.entity.CardEntity;
import com.example.bank_rest.entity.CardStatus;
import com.example.bank_rest.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    @Query(value = """
    SELECT * FROM cards c
        WHERE (:userId IS NULL OR c.user_id=:userId)
        AND (:id IS NULL OR c.id=:id)
        AND (:numberCard IS NULL OR c.encrypted_number=:numberCard)
        AND (:balance IS NULL OR c.balance=:balance)
    """, nativeQuery = true)
    Page<CardEntity> findAllByFilter(
            @Param("userId") Long userId,
            @Param("id") Long id,
            @Param("numberCard") String numberCard,
            @Param("balance") Long balance,
            Pageable pageable
    );
}
