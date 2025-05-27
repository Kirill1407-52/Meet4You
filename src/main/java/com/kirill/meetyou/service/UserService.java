package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.dto.BulkResponse;
import com.kirill.meetyou.dto.UserCreateDto;
import com.kirill.meetyou.dto.UserUpdateDto;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserCache cache;

    public UserService(UserRepository userRepository, UserCache cache) {
        this.userRepository = userRepository;
        this.cache = cache;
    }

    public List<User> findAll() {
        try {
            log.debug("Fetching all users");
            return userRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch all users. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при получении пользователей");
        }
    }

    public Optional<User> findById(Long id) {
        try {
            if (id == null || id <= 0) {
                log.warn("Invalid user ID requested: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный ID пользователя");
            }

            User cachedUser = cache.get(id);
            if (cachedUser != null) {
                log.info("User {} retrieved from cache", id);
                return Optional.of(cachedUser);
            }

            Optional<User> userOptional = userRepository.findById(id);
            userOptional.ifPresent(user -> cache.put(id, user));
            return userOptional;
        } catch (Exception e) {
            log.error("Failed to find user with ID: {}. Error: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при поиске пользователя");
        }
    }

    public User create(User user) {
        try {
            validateUserForCreation(user);
            user.setAge(Period.between(user.getBirth(), LocalDate.now()).getYears());

            User savedUser = userRepository.save(user);
            cache.put(savedUser.getId(), savedUser);
            return savedUser;
        } catch (Exception e) {
            log.error("Failed to create user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ошибка при создании пользователя");
        }
    }

    public void delete(Long id) {
        try {
            validateUserId(id);
            if (!userRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
            }

            userRepository.deleteById(id);
            cache.remove(id);
        } catch (Exception e) {
            log.error("Failed to delete user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении пользователя");
        }
    }

    @Transactional
    public User update(Long id, UserUpdateDto dto) {
        try {
            validateUserId(id);

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

            if (dto.getEmail() != null && !dto.getEmail().trim().equals(user.getEmail())) {
                if (dto.getEmail().trim().isEmpty() || dto.getEmail().equalsIgnoreCase("null")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
                }
                if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email уже используется");
                }
                user.setEmail(dto.getEmail().trim());
            }

            if (dto.getName() != null) {
                if (dto.getName().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя не может быть пустым");
                }
                user.setName(dto.getName().trim());
            }

            User updatedUser = userRepository.save(user);
            cache.put(id, updatedUser);
            return updatedUser;
        } catch (Exception e) {
            log.error("Failed to update user. Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при обновлении пользователя");
        }
    }

    @Transactional
    public BulkResponse bulkCreate(List<UserCreateDto> userDtos) {
        BulkResponse.BulkResponseBuilder response = BulkResponse.builder()
                .successCount(0)
                .failCount(0)
                .errors(new ArrayList<>());

        if (userDtos == null || userDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список пользователей не может быть пустым");
        }

        for (UserCreateDto dto : userDtos) {
            try {
                User user = new User();
                user.setEmail(dto.getEmail());
                user.setName(dto.getName());
                user.setBirth(dto.getBirth());
                validateUserForCreation(user);
                user.setAge(Period.between(dto.getBirth(), LocalDate.now()).getYears());

                User saved = userRepository.save(user);
                cache.put(saved.getId(), saved);
                response.successCount(response.build().getSuccessCount() + 1);
            } catch (Exception e) {
                response.failCount(response.build().getFailCount() + 1);
                response.errors(List.of("Ошибка для email " + dto.getEmail() + ": " + e.getMessage()));
            }
        }

        return response.build();
    }

    // === Helpers ===

    private void validateUserForCreation(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()
                || user.getEmail().equalsIgnoreCase("null")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email обязателен");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким email уже существует");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя обязательно");
        }

        if (user.getBirth() == null || user.getBirth().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректная дата рождения");
        }
    }

    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный ID пользователя");
        }
    }
}
