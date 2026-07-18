package com.example.bank_rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

@Component
public class EncryptionService {
    private final TextEncryptor encryptor;

    public EncryptionService(
            @Value("${crypt.password}") String password,
            @Value("${crypt.salt}") String salt
    ) {
        this.encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String raw) {
        return encryptor.encrypt(raw);
    }

    public String decrypt(String encrypted) {
        return encryptor.decrypt(encrypted);
    }
}
