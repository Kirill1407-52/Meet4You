package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.InterestRepository;
import com.kirill.meetyou.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {
    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String INTEREST_ALREADY_EXISTS = "Интерес уже существует";
    private static final String INTEREST_NOT_FOUND = "Интерес не найден";
    private static final String INTEREST_ADDED = "Интерес '%s' добавлен пользователю %d";
    private static final String INTEREST_REMOVED = "Интерес '%s' удален у пользователя %d";

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final UserCache userCache;

    @Transactional
    public void addInterestToUser(Long userId, String interestType) {
        log.info("Добавление интереса '{}' пользователю {}", interestType, userId);

        validateInterestName(interestType);
        User user = getUserById(userId);

        Interest interest = interestRepository.findByInterestType(interestType)
                .orElseGet(() -> createNewInterest(interestType));

        if (user.getInterests().contains(interest)) {
            log.warn("Попытка добавить существующий интерес: {}", interestType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "У пользователя уже есть этот интерес");
        }

        user.getInterests().add(interest);
        User updatedUser = userRepository.save(user);
        userCache.put(userId, updatedUser);

        log.info(String.format(INTEREST_ADDED, interestType, userId));
    }

    @Transactional
    public void removeInterestFromUser(Long userId, String interestName) {
        log.info("Удаление интереса '{}' у пользователя {}", interestName, userId);

        validateInterestName(interestName);
        User user = getUserById(userId);
        Interest interest = getInterestByName(interestName);

        if (!user.getInterests().contains(interest)) {
            log.warn("Попытка удалить отсутствующий интерес: {}", interestName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "У пользователя нет этого интереса");
        }

        user.getInterests().remove(interest);
        User updatedUser = userRepository.save(user);
        userCache.put(userId, updatedUser);

        log.info(String.format(INTEREST_REMOVED, interestName, userId));
    }

    @Transactional
    public Interest updateInterest(Long userId, Long interestId, Interest updatedInterest) {
        validateInterestName(updatedInterest.getInterestType());
        User user = getUserById(userId);
        Interest existingInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, INTEREST_NOT_FOUND));

        // Проверка, что этот интерес привязан к пользователю
        if (!user.getInterests().contains(existingInterest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Интерес не принадлежит пользователю");
        }

        // Проверка, не используется ли новое имя другим интересом
        if (interestRepository.existsByInterestType(updatedInterest.getInterestType()) &&
                !existingInterest.getInterestType().equals(updatedInterest.getInterestType())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, INTEREST_ALREADY_EXISTS);
        }

        existingInterest.setInterestType(updatedInterest.getInterestType().trim());
        return interestRepository.save(existingInterest);
    }

    @Transactional
    public void updateUserInterest(Long userId, Long interestId, String newInterestName) {
        validateInterestName(newInterestName);
        User user = getUserById(userId);

        Interest oldInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, INTEREST_NOT_FOUND));

        if (!user.getInterests().contains(oldInterest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Интерес не принадлежит пользователю");
        }

        // Удаляем старый интерес у пользователя
        user.getInterests().remove(oldInterest);

        // Ищем или создаем новый интерес
        Interest newInterest = interestRepository.findByInterestType(newInterestName)
                .orElseGet(() -> createNewInterest(newInterestName));

        // Добавляем новый интерес пользователю
        user.getInterests().add(newInterest);

        userRepository.save(user);
        userCache.put(userId, user);
    }


    @Transactional(readOnly = true)
    public Set<Interest> getUserInterests(Long userId) {
        log.debug("Получение интересов пользователя {}", userId);
        return getUserById(userId).getInterests();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(USER_NOT_FOUND + ": {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
                });
    }

    private Interest getInterestByName(String interestName) {
        return interestRepository.findByInterestType(interestName)
                .orElseThrow(() -> {
                    log.warn(INTEREST_NOT_FOUND + ": {}", interestName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, INTEREST_NOT_FOUND);
                });
    }

    private Interest createNewInterest(String interestType) {
        if (interestRepository.existsByInterestType(interestType)) {
            log.warn(INTEREST_ALREADY_EXISTS + ": {}", interestType);
            throw new ResponseStatusException(HttpStatus.CONFLICT, INTEREST_ALREADY_EXISTS);
        }

        Interest newInterest = new Interest();
        newInterest.setInterestType(interestType.trim());
        return interestRepository.save(newInterest);
    }

    private void validateInterestName(String interestName) {
        if (interestName == null || interestName.trim().isEmpty()) {
            log.warn("Пустое название интереса");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Название интереса не может быть пустым");
        }
    }
}