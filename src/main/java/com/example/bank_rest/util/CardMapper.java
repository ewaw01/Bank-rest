package com.example.bank_rest.util;

import com.example.bank_rest.dto.Card;
import com.example.bank_rest.entity.CardEntity;
import com.example.bank_rest.service.EncryptionService;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    private final EncryptionService encryptionService;

    public CardMapper(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public Card toDto(CardEntity cardEntity) {
        return new Card(
                cardEntity.getId(),
                getMaskedNumber(cardEntity.getEncryptedNumber()),
                cardEntity.getOwner(),
                cardEntity.getExpiryDate(),
                cardEntity.getStatus(),
                cardEntity.getBalance(),
                cardEntity.getUser().getId()
        );
    }

    private String getMaskedNumber(String encryptedNumber) {
        if (encryptedNumber == null || encryptedNumber.isEmpty()) {
            return "**** **** **** ****";
        }

        String rawNumber = encryptionService.decrypt(encryptedNumber);
        String last4 = rawNumber.substring(rawNumber.length() - 4);

        return "**** **** **** " + last4;
    }
}
