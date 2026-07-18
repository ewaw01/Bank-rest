package com.example.bank_rest.service;

import com.example.bank_rest.dto.Card;
import com.example.bank_rest.dto.CardSearchFilter;
import com.example.bank_rest.dto.User;
import com.example.bank_rest.dto.UserSearchFilter;
import com.example.bank_rest.entity.CardEntity;
import com.example.bank_rest.entity.UserEntity;
import com.example.bank_rest.repository.CardRepository;
import com.example.bank_rest.repository.UserRepository;
import com.example.bank_rest.util.CardMapper;
import com.example.bank_rest.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserMapper userMapper;
    private final CardMapper cardMapper;

    public List<User> searchAllUsersByFilter(
            UserSearchFilter filter
    ) {
        log.info("called method searchAllUsersByFilter");

        Integer pageNum = filter.pageNum() == null ? 0 : filter.pageNum();
        Integer pageSize = filter.pageSize() == null ? 5 : filter.pageSize();

        var pageable = PageRequest.of(pageNum, pageSize);

        Page<UserEntity> pageResult = userRepository.findAllByFilter(
                filter.id(),
                filter.email(),
                filter.username(),
                pageable
        );

        return pageResult.stream()
                .map(userMapper::toUser)
                .toList();
    }

    public void deleteUser(
            Long id,
            UserDetails currentUser
    ) {
        log.info("called method deleteUser");

        UserEntity existingUser = userRepository.findById(id).orElseThrow(NoSuchElementException::new);
        boolean isSelf = currentUser.getUsername().equals(existingUser.getEmail());
        boolean isAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isSelf && !isAdmin) {
            throw new SecurityException("You can only delete your own profile");
        }

        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUser(
            User updatedUser,
            UserDetails currentUser
    ) {
        log.info("called method updateUser");

        UserEntity user = userRepository.findById(updatedUser.id()).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );

        boolean isSelf = currentUser.getUsername().equals(user.getEmail());
        boolean isAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new SecurityException("You can only update your own profile");
        }

        user.setUsernameSafe(updatedUser.username());
        user.setIsBlockedSafe(updatedUser.isBlocked());

        userRepository.save(user);

        return userMapper.toUser(user);
    }

    public List<Card> getUserCards(
            Long userId,
            CardSearchFilter filter,
            UserDetails currentUser
    ) {
        log.info("called method getUserCards");

        boolean isAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin && userId == null) {
            throw new SecurityException("If you are not an admin, you must specify a user id!");
        }

        if(userId != null) {
            UserEntity user = userRepository.findById(userId).orElseThrow(
                    () -> new NoSuchElementException("User with id " + userId + " not found")
            );
            boolean isSelf = currentUser.getUsername().equals(user.getEmail());
            if (!isSelf && !isAdmin) {
                throw new SecurityException("You can view only your own profile");
            }
        }

        Integer pageNum = filter.pageNum() == null ? 0 : filter.pageNum();
        Integer pageSize = filter.pageSize() == null ? 5 : filter.pageSize();

        var pageable = PageRequest.of(pageNum, pageSize);

        Page<CardEntity> pageResult = cardRepository.findAllByFilter(
                userId,
                filter.id(),
                filter.numberCard(),
                filter.balance(),
                pageable
        );

        return pageResult.stream()
                .map(cardMapper::toDto)
                .toList();
    }

    private static PageRequest getPageRequest(CardSearchFilter filter, UserDetails currentUser, UserEntity user) {
        boolean isSelf = currentUser.getUsername().equals(user.getEmail());
        boolean isAdmin = currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isSelf && !isAdmin) {
            throw new SecurityException("You can only get your own profile");
        }

        Integer pageNum = filter.pageNum() == null ? 0 : filter.pageNum();
        Integer pageSize = filter.pageSize() == null ? 5 : filter.pageSize();

        var pageable = PageRequest.of(pageNum, pageSize);
        return pageable;
    }

    @Transactional
    public void blockUser(Long id) {
        log.info("called method blockUser");

        UserEntity user = userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("User with id " + id + " not found")
        );

        user.setIsBlocked(true);
        userRepository.save(user);
    }
}
