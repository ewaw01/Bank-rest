package com.example.bank_rest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "username")
    private String username;
    @Column(name = "is_blocked")
    private Boolean isBlocked;
    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CardEntity> cards = new ArrayList<>();

    public void addCard(CardEntity card) {
        cards.add(card);
        card.setUser(this);
    }
    public void removeCard(CardEntity card) {
        cards.remove(card);
        card.setUser(null);
    }

    public void setUsernameSafe(String username) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
    }

    public void setIsBlockedSafe(Boolean blocked) {
        if (blocked != null) {
            this.isBlocked = blocked;
        }
    }
}
