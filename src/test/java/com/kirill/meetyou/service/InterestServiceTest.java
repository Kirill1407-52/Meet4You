package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.model.Interest;
import com.kirill.meetyou.repository.InterestRepository;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private InterestService interestService;

    private User testUser;
    private Interest existingInterest;
    private Interest newInterest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setInterests(new HashSet<>());

        existingInterest = new Interest();
        existingInterest.setId(1L);
        existingInterest.setInterestType("Sports");

        newInterest = new Interest();
        newInterest.setId(2L);
        newInterest.setInterestType("Music");
    }

    @Test
    void addInterestToUser_NewInterest_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.empty());
        when(interestRepository.existsByInterestType(anyString())).thenReturn(false);
        when(interestRepository.save(any(Interest.class))).thenReturn(newInterest);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        interestService.addInterestToUser(1L, "Music");

        verify(interestRepository).save(any(Interest.class));
        verify(userRepository).save(testUser);
        verify(userCache).put(1L, testUser);
        assertTrue(testUser.getInterests().contains(newInterest));
    }

    @Test
    void addInterestToUser_ExistingInterest_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.of(existingInterest));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        interestService.addInterestToUser(1L, "Sports");

        verify(interestRepository, never()).save(any(Interest.class));
        verify(userRepository).save(testUser);
        verify(userCache).put(1L, testUser);
        assertTrue(testUser.getInterests().contains(existingInterest));
    }

    @Test
    void addInterestToUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.addInterestToUser(1L, "Sports"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь не найден", exception.getReason());
    }

    @Test
    void addInterestToUser_EmptyInterestName_ThrowsException() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.addInterestToUser(1L, "  "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Название интереса не может быть пустым", exception.getReason());
    }

    @Test
    void addInterestToUser_DuplicateInterest_ThrowsException() {
        testUser.getInterests().add(existingInterest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.of(existingInterest));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.addInterestToUser(1L, "Sports"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("У пользователя уже есть этот интерес", exception.getReason());
    }

    @Test
    void removeInterestFromUser_Success() {
        testUser.getInterests().add(existingInterest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.of(existingInterest));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        interestService.removeInterestFromUser(1L, "Sports");

        verify(userRepository).save(testUser);
        verify(userCache).put(1L, testUser);
        assertFalse(testUser.getInterests().contains(existingInterest));
    }

    @Test
    void removeInterestFromUser_InterestNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.removeInterestFromUser(1L, "Unknown"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Интерес не найден", exception.getReason());
    }

    @Test
    void removeInterestFromUser_UserDoesNotHaveInterest_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.of(existingInterest));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.removeInterestFromUser(1L, "Sports"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("У пользователя нет этого интереса", exception.getReason());
    }

    @Test
    void getUserInterests_Success() {
        Set<Interest> interests = new HashSet<>();
        interests.add(existingInterest);
        testUser.setInterests(interests);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        Set<Interest> result = interestService.getUserInterests(1L);

        assertEquals(1, result.size());
        assertTrue(result.contains(existingInterest));
    }

    @Test
    void getUserInterests_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.getUserInterests(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь не найден", exception.getReason());
    }

    @Test
    void addInterestToUser_InterestAlreadyExistsInDB_ThrowsConflictException() {
        // Подготовка
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.empty());
        when(interestRepository.existsByInterestType(anyString())).thenReturn(true);

        // Действие и проверка
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> interestService.addInterestToUser(1L, "Sports"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Интерес уже существует", exception.getReason());
        verify(interestRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addInterestToUser_VerifyMethodCallOrder() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(interestRepository.findByInterestType(anyString())).thenReturn(Optional.empty());
        when(interestRepository.existsByInterestType(anyString())).thenReturn(false);
        when(interestRepository.save(any())).thenReturn(existingInterest);
        when(userRepository.save(any())).thenReturn(testUser);

        interestService.addInterestToUser(1L, "Sports");

        InOrder inOrder = inOrder(userRepository, interestRepository);
        inOrder.verify(userRepository).findById(1L);
        inOrder.verify(interestRepository).findByInterestType("Sports");
        inOrder.verify(interestRepository).existsByInterestType("Sports");
        inOrder.verify(interestRepository).save(any());
        inOrder.verify(userRepository).save(testUser);
    }
}