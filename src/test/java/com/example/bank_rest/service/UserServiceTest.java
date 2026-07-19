package com.example.bank_rest.service;

import com.example.bank_rest.dto.User;
import com.example.bank_rest.dto.UserSearchFilter;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.repository.CardRepository;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.util.CardMapper;
import com.example.bank_rest.util.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserService userService;

    private UserDetails mockUserDetails(String email, String role) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("password")
                .authorities(role)
                .build();
    }

    @Test
    @DisplayName("поиск пользователей с фильтром - успешно")
    void searchAllUsersByFilter_Success() {
        UserSearchFilter filter = new UserSearchFilter(1L, "test@test.com", "testuser", 0, 5);
        PageRequest pageable = PageRequest.of(0, 5);
        Page<UserEntity> pageResult = new PageImpl<>(List.of(new UserEntity()));

        Mockito.when(userRepository.findAllByFilter(filter.id(), filter.email(), filter.username(), pageable))
                .thenReturn(pageResult);
        Mockito.when(userMapper.toUser(Mockito.any(UserEntity.class))).thenReturn(new User(1L, null, null, null, null, null));

        List<User> result = userService.searchAllUsersByFilter(filter);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        Mockito.verify(userRepository, Mockito.times(1))
                .findAllByFilter(filter.id(), filter.email(), filter.username(), pageable);
    }

    @Test
    @DisplayName("удаление пользователя - владелец удаляет себя")
    void deleteUser_Self_Success() {
        Long userId = 1L;
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email, "ROLE_USER");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail(email);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.deleteUser(userId, currentUser));

        Mockito.verify(userRepository, Mockito.times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("удаление пользователя - не владелец и не админ, ошибка")
    void deleteUser_NotSelfAndNotAdmin_ThrowsException() {
        Long userId = 1L;
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email, "ROLE_USER");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("other@test.com");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class, () -> userService.deleteUser(userId, currentUser));
        Mockito.verify(userRepository, Mockito.never()).deleteById(userId);
    }

    @Test
    @DisplayName("обновление пользователя - успешно")
    void updateUser_Success() {
        Long userId = 1L;
        String email = "test@test.com";
        UserDetails currentUser = mockUserDetails(email, "ROLE_USER");

        User updatedUser = new User(userId, "new@test.com", null, "newUsername", false, null);
        UserEntity existingUser = new UserEntity();
        existingUser.setId(userId);
        existingUser.setEmail(email);
        existingUser.setUsername("oldUsername");

        UserEntity savedUser = new UserEntity();
        savedUser.setId(userId);
        savedUser.setEmail("new@test.com");
        savedUser.setUsername("newUsername");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(savedUser);
        Mockito.when(userMapper.toUser(Mockito.any(UserEntity.class)))
                .thenReturn(new User(userId, "new@test.com", null, "newUsername", false, null));

        User result = userService.updateUser(updatedUser, currentUser);

        assertNotNull(result);
        assertEquals("newUsername", result.username());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserEntity.class));
    }

    @Test
    @DisplayName("блокировка пользователя - успешно")
    void blockUser_Success() {
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setIsBlocked(false);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockUser(userId);

        assertTrue(user.getIsBlocked());
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }
}
