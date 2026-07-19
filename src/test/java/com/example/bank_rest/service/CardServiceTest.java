package com.example.bank_rest.service;

import com.example.bank_rest.dto.CardRequestDto;
import com.example.bank_rest.dto.TransferRequestDto;
import com.example.bank_rest.entity.CardEntity;
import com.example.bank_rest.entity.CardStatus;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.repository.CardRepository;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.util.CardMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    private UserDetails mockUserDetails(String email) {
        return User.builder()
                .username(email)
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("создание карты - успешно")
    void issueCardForUser_Success() {
        Long userId = 1L;
        CardRequestDto request = new CardRequestDto(userId, LocalDate.of(2028, 12, 31));
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("testUser");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(encryptionService.encrypt(Mockito.anyString())).thenReturn("encryptedNumber");
        Mockito.when(cardRepository.save(Mockito.any(CardEntity.class))).thenReturn(new CardEntity());
        Mockito.when(cardMapper.toDto(Mockito.any(CardEntity.class))).thenReturn(null);

        assertDoesNotThrow(() -> cardService.issueCardForUser(request));

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(cardRepository, Mockito.times(1)).save(Mockito.any(CardEntity.class));
    }

    @Test
    @DisplayName("создание карты - пользователь не найден, ошибка")
    void issueCardForUser_UserNotFound_ThrowsException() {
        Long userId = 1L;
        CardRequestDto request = new CardRequestDto(userId, LocalDate.of(2028, 12, 31));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> cardService.issueCardForUser(request));
    }

    @Test
    @DisplayName("удаление карты - успешно")
    void deleteCard_Success() {
        Long cardId = 1L;
        UserEntity user = new UserEntity();
        user.setId(1L);

        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setUser(user);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertDoesNotThrow(() -> cardService.deleteCard(cardId));

        Mockito.verify(cardRepository, Mockito.times(1)).findById(cardId);
        assertTrue(user.getCards().isEmpty());
    }

    @Test
    @DisplayName("блокировка карты администратором - успешно")
    void blockCard_Success() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        Mockito.verify(cardRepository, Mockito.times(1)).findById(cardId);
    }

    @Test
    @DisplayName("запрос на блокировку карты - пользователь владелец")
    void requestBlock_Success() {
        Long cardId = 1L;
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email);

        UserEntity user = new UserEntity();
        user.setEmail(email);

        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.requestBlock(cardId, currentUser);

        assertEquals(CardStatus.BLOCK_REQUESTED, card.getStatus());
        Mockito.verify(cardRepository, Mockito.times(1)).findById(cardId);
    }

    @Test
    @DisplayName("запрос на блокировку карты - не владелец, ошибка")
    void requestBlock_NotOwner_ThrowsException() {
        Long cardId = 1L;
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email);

        UserEntity user = new UserEntity();
        user.setEmail("other@test.com");

        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(SecurityException.class, () -> cardService.requestBlock(cardId, currentUser));
    }

    @Test
    @DisplayName("активация карты администратором - успешно")
    void activateCard_Success() {
        Long cardId = 1L;
        CardEntity card = new CardEntity();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        Mockito.when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.activateCard(cardId);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        Mockito.verify(cardRepository, Mockito.times(1)).save(card);
    }

    @Test
    @DisplayName("перевод между картами - успешно")
    void transferBetweenCards_Success() {
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email);

        UserEntity user = new UserEntity();
        user.setEmail(email);

        CardEntity fromCard = new CardEntity();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));
        fromCard.setBalance(1000L);

        CardEntity toCard = new CardEntity();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));
        toCard.setBalance(500L);

        TransferRequestDto request = new TransferRequestDto(1L, 2L, 200L);

        Mockito.when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        cardService.transferBetweenCards(request, currentUser);

        assertEquals(800L, fromCard.getBalance());
        assertEquals(700L, toCard.getBalance());
        Mockito.verify(cardRepository, Mockito.times(2)).save(Mockito.any(CardEntity.class));
    }

    @Test
    @DisplayName("перевод между картами - недостаточно средств, ошибка")
    void transferBetweenCards_InsufficientBalance_ThrowsException() {
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email);

        UserEntity user = new UserEntity();
        user.setEmail(email);

        CardEntity fromCard = new CardEntity();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpiryDate(LocalDate.now().plusYears(1));
        fromCard.setBalance(100L);

        CardEntity toCard = new CardEntity();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpiryDate(LocalDate.now().plusYears(1));
        toCard.setBalance(500L);

        TransferRequestDto request = new TransferRequestDto(1L, 2L, 200L);

        Mockito.when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        Mockito.when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () -> cardService.transferBetweenCards(request, currentUser));
    }
}
