package com.example.bank_rest.util;

import com.example.bank_rest.dto.User;
import com.example.bank_rest.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    @Autowired
    private final CardMapper cardMapper;

    public UserMapper(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    public User toUser(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getRole(),
                userEntity.getUsername(),
                userEntity.getIsBlocked(),
                userEntity.getCards().stream()
                        .map(cardMapper::toDto)
                        .toList()
        );
    }
}
