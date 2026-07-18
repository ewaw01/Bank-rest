package com.example.bank_rest.controller;

import com.example.bank_rest.dto.*;
import com.example.bank_rest.service.CardService;
import com.example.bank_rest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank")
public class MainController {
    private final Logger log = LoggerFactory.getLogger(MainController.class);
    private final UserService userService;
    private final CardService cardService;

    @GetMapping("/admin/user")
    public ResponseEntity<List<User>> findUserByFilter(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "page_num", required = false) Integer pageNum,
            @RequestParam(name = "page_size", required = false) Integer pageSize
    ) {
        log.info("called method findUserByFilter");

        UserSearchFilter filter = new UserSearchFilter(
                id,
                email,
                username,
                pageNum,
                pageSize
        );

        return ResponseEntity.ok().body(userService.searchAllUsersByFilter(filter));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("called method deleteUser");

        userService.deleteUser(id, currentUser);
        return ResponseEntity.ok().body("User with id " + id + " was deleted.");
    }

    @PutMapping("/user")
    public ResponseEntity<User> updateUser(
            @RequestBody @Valid User updatedUser,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("called method updateUser");

        return ResponseEntity.ok().body(userService.updateUser(updatedUser, currentUser));
    }

    @PostMapping("/admin/card")
    public ResponseEntity<Card> addCard(
            @Valid @RequestBody CardRequestDto card
    ) {
        log.info("called method addCard");

        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.issueCardForUser(card));
    }

    @DeleteMapping("/admin/card/{id}")
    public ResponseEntity<String> deleteCard(
            @PathVariable("id") Long id
    ) {
        log.info("called method deleteCard");

        cardService.deleteCard(id);
        return ResponseEntity.ok().body("Card with id " + id + " was deleted.");
    }

    @PutMapping("/card/{id}/block-request")
    public ResponseEntity<String> requestBlock(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("called method requestBlock");

        cardService.requestBlock(id, currentUser);
        return ResponseEntity.ok().body("Successfully requested block.");
    }

    @PutMapping("/admin/card/{id}/block")
    public ResponseEntity<String> blockCard(
            @PathVariable Long id
    ) {
        log.info("called method changeCardStatus");

        cardService.blockCard(id);
        return ResponseEntity.ok().body("Card with id " + id + " was blocked.");
    }

    @PutMapping("/admin/card/{id}/activate")
    public ResponseEntity<String> activateCard(
            @PathVariable Long id
    ) {
        log.info("called method activateCard");

        cardService.activateCard(id);
        return ResponseEntity.ok().body("Card with id " + id + " was activated.");
    }

    @GetMapping("/user/card")
    public ResponseEntity<List<Card>> getUserCards(
            @RequestParam(name = "user_id", required = false) Long userId,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "number_card", required = false) String numberCard,
            @RequestParam(name = "balance", required = false) Long balance,
            @RequestParam(name = "page_size", required = false) Integer pageSize,
            @RequestParam(name = "page_num", required = false) Integer pageNum,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("called method getUserCards");

        CardSearchFilter filter = new CardSearchFilter(
                id,
                numberCard,
                balance,
                pageSize,
                pageNum
        );

        return ResponseEntity.ok().body(userService.getUserCards(userId, filter, currentUser));
    }

    @PostMapping("/card/transfer")
    public ResponseEntity<String> transferBetweenCards(
            @Valid @RequestBody TransferRequestDto request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("called method transferBetweenCards");

        cardService.transferBetweenCards(request, currentUser);
        return ResponseEntity.ok().body("Transfer completed successfully");
    }

    @PutMapping("/admin/user/{id}/block")
    public ResponseEntity<String> blockUser(
            @PathVariable Long id
    ) {
        log.info("called method blockUser");

        userService.blockUser(id);
        return ResponseEntity.ok().body("User with id " + id + " was blocked.");
    }

}
