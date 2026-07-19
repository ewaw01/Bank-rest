package com.example.bank_rest.controller;

import com.example.bank_rest.dto.*;
import com.example.bank_rest.entity.CardStatus;
import com.example.bank_rest.service.CardService;
import com.example.bank_rest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CardService cardService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/bank/admin/user - успешный поиск пользователей (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findUserByFilter_Success() throws Exception {
        List<User> users = List.of(new User(1L, "test@test.com", null, "testuser", false, null));

        when(userService.searchAllUsersByFilter(any(UserSearchFilter.class))).thenReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/admin/user")
                        .param("id", "1")
                        .param("email", "test@test.com")
                        .param("username", "testuser")
                        .param("page_num", "0")
                        .param("page_size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/bank/user/{id} - успешное удаление пользователя")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteUser_Success() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(eq(userId), any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/bank/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("User with id 1 was deleted."));
    }

    @Test
    @DisplayName("PUT /api/bank/user - успешное обновление пользователя")
    @WithMockUser(username = "user", roles = {"USER"})
    void updateUser_Success() throws Exception {
        User updatedUser = new User(1L, "new@test.com", null, "newUsername", false, null);

        when(userService.updateUser(any(User.class), any())).thenReturn(updatedUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/bank/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("newUsername"));
    }

    @Test
    @DisplayName("POST /api/bank/admin/card - успешное создание карты (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addCard_Success() throws Exception {
        CardRequestDto request = new CardRequestDto(1L, LocalDate.of(2028, 12, 31));
        Card card = new Card(1L, "**** **** **** 1234", "testuser", LocalDate.of(2028, 12, 31), CardStatus.ACTIVE, 0L, 1L);

        when(cardService.issueCardForUser(any(CardRequestDto.class))).thenReturn(card);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/admin/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.owner").value("testuser"));
    }

    @Test
    @DisplayName("DELETE /api/bank/admin/card/{id} - успешное удаление карты (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCard_Success() throws Exception {
        Long cardId = 1L;

        doNothing().when(cardService).deleteCard(cardId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/bank/admin/card/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Card with id 1 was deleted."));
    }

    @Test
    @DisplayName("PUT /api/bank/card/{id}/block-request - успешный запрос на блокировку карты")
    @WithMockUser(username = "user", roles = {"USER"})
    void requestBlock_Success() throws Exception {
        Long cardId = 1L;

        doNothing().when(cardService).requestBlock(eq(cardId), any());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/bank/card/{id}/block-request", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Successfully requested block."));
    }

    @Test
    @DisplayName("PUT /api/bank/admin/card/{id}/block - успешная блокировка карты (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blockCard_Success() throws Exception {
        Long cardId = 1L;

        doNothing().when(cardService).blockCard(cardId);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/bank/admin/card/{id}/block", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Card with id 1 was blocked."));
    }

    @Test
    @DisplayName("PUT /api/bank/admin/card/{id}/activate - успешная активация карты (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void activateCard_Success() throws Exception {
        Long cardId = 1L;

        doNothing().when(cardService).activateCard(cardId);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/bank/admin/card/{id}/activate", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Card with id 1 was activated."));
    }

    @Test
    @DisplayName("GET /api/bank/user/card - успешное получение карт пользователя")
    @WithMockUser(username = "user", roles = {"USER"})
    void getUserCards_Success() throws Exception {
        Long userId = 1L;
        List<Card> cards = List.of(new Card(1L, "**** **** **** 1234", "testuser", LocalDate.of(2028, 12, 31), CardStatus.ACTIVE, 0L, 1L));

        when(userService.getUserCards(eq(userId), any(CardSearchFilter.class), any())).thenReturn(cards);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/bank/user/card")
                        .param("user_id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("POST /api/bank/card/transfer - успешный перевод между картами")
    @WithMockUser(username = "user", roles = {"USER"})
    void transferBetweenCards_Success() throws Exception {
        TransferRequestDto request = new TransferRequestDto(1L, 2L, 100L);

        doNothing().when(cardService).transferBetweenCards(any(TransferRequestDto.class), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bank/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Transfer completed successfully"));
    }

    @Test
    @DisplayName("PUT /api/bank/admin/user/{id}/block - успешная блокировка пользователя (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void blockUser_Success() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).blockUser(userId);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/bank/admin/user/{id}/block", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("User with id 1 was blocked."));
    }
}
