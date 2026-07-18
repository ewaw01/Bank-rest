package com.example.bank_rest.service;

import com.example.bank_rest.dto.Card;
import com.example.bank_rest.dto.CardRequestDto;
import com.example.bank_rest.dto.TransferRequestDto;
import com.example.bank_rest.entity.CardEntity;
import com.example.bank_rest.entity.CardStatus;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.repository.CardRepository;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.util.CardMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {
    private final Logger log = LoggerFactory.getLogger(CardService.class);
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final EncryptionService encryptionService;

    @Transactional
    public Card issueCardForUser(CardRequestDto cardRequest) {
        log.info("called method issueCardForUser");

        UserEntity user = userRepository.findById(cardRequest.ownerId())
                .orElseThrow(() -> new NoSuchElementException("User with id " + cardRequest.ownerId() + " not found"));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setEncryptedNumber(generateEncryptedNumber());
        cardEntity.setOwner(user.getUsername());
        cardEntity.setExpiryDate(cardRequest.expiryDate());
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntity.setBalance(0L);
        cardEntity.setUser(user);

        user.addCard(cardEntity);

        cardRepository.save(cardEntity);
        userRepository.save(user);


        return cardMapper.toDto(cardEntity);
    }

    private String generateEncryptedNumber() {
        String number = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return encryptionService.encrypt(number);
    }

    @Transactional
    public void deleteCard(Long id) {
        log.info("called method deleteCard");

        CardEntity card = cardRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Card with id " + id + " not found")
        );
        UserEntity user = card.getUser();
        user.removeCard(card);
    }

    @Transactional
    public void blockCard(Long id) {
        log.info("called method blockCard");

        CardEntity card = cardRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Card with id " + id + " not found")
        );

        card.setStatus(CardStatus.BLOCKED);
    }

    @Transactional
    public void requestBlock(
            Long cardId,
            UserDetails currentUser
    ) {
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NoSuchElementException("Card with id " + cardId + " not found"));

        if (!card.getUser().getEmail().equals(currentUser.getUsername())) {
            throw new SecurityException("You can only request block for your own cards");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Only active cards can be blocked");
        }

        card.setStatus(CardStatus.BLOCK_REQUESTED);
    }

    @Transactional
    public void transferBetweenCards(TransferRequestDto request, UserDetails currentUser) {
        log.info("called method transferBetweenCards");

        CardEntity fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new NoSuchElementException("From card not found"));
        CardEntity toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new NoSuchElementException("To card not found"));

        String email = currentUser.getUsername();
        if (!fromCard.getUser().getEmail().equals(email) ||
                !toCard.getUser().getEmail().equals(email)) {
            throw new SecurityException("Both cards must belong to you");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("From card is not active");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("To card is not active");
        }

        if (fromCard.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("From card has expired");
        }
        if (toCard.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("To card has expired");
        }

        Long amount = request.amount();
        if (fromCard.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance on from card");
        }

        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalStateException("Cannot transfer to the same card");
        }

        fromCard.setBalance(fromCard.getBalance() - amount);
        toCard.setBalance(toCard.getBalance() + amount);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    /*
    осталось проверить, правильно ли работают трансферы,
    добавить, чтобы в проверке на expired, если истек, статус карты менялся на EXPIRED,
    добавить, чтобы ADMIN мог обратно активировать карты,
    решить, что делать с логикой блокировки пользователя,
    Добавить: unit-тесты, миграции, docker-compose
     */

}
