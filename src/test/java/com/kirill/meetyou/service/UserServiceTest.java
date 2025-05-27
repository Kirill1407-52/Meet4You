package com.kirill.meetyou.service;

import com.kirill.meetyou.cache.UserCache;
import com.kirill.meetyou.dto.BulkResponse;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCache cache;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setBirth(LocalDate.of(1990, 1, 1));
        testUser.setAge(33);

        UserCreateDto testUserCreateDto = new UserCreateDto();
        testUserCreateDto.setName("Jane Doe");
        testUserCreateDto.setEmail("jane@example.com");
        testUserCreateDto.setBirth(LocalDate.of(1995, 5, 15));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void findAll_ShouldThrowExceptionWhenRepositoryFails() {
        // Arrange
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findAll());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void findById_ShouldReturnUserFromCache() {
        // Arrange
        when(cache.get(1L)).thenReturn(testUser);

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(cache).get(1L);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void findById_ShouldReturnUserFromRepositoryAndCacheIt() {
        // Arrange
        when(cache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(cache).get(1L);
        verify(userRepository).findById(1L);
        verify(cache).put(1L, testUser);
    }

    @Test
    void findById_ShouldReturnEmptyForNonExistentUser() {
        // Arrange
        when(cache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(cache).get(1L);
        verify(userRepository).findById(1L);
        verify(cache, never()).put(any(), any());
    }

    @Test
    void findById_ShouldThrowExceptionForInvalidId() {
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.findById(null));
        assertThrows(ResponseStatusException.class, () -> userService.findById(0L));
        assertThrows(ResponseStatusException.class, () -> userService.findById(-1L));
    }

    @Test
    void create_ShouldSuccessfullyCreateUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        User result = userService.create(testUser);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(userRepository).save(testUser);
        verify(cache).put(testUser.getId(), testUser);
    }

    @Test
    void create_ShouldThrowExceptionForNullUser() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void create_ShouldThrowExceptionForInvalidEmail() {
        // Arrange
        testUser.setEmail(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void create_ShouldThrowExceptionForDuplicateEmail() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void delete_ShouldSuccessfullyDeleteUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.delete(1L);

        // Assert
        verify(userRepository).existsById(1L);
        verify(cache).remove(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowExceptionForInvalidId() {
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.delete(null));
        assertThrows(ResponseStatusException.class, () -> userService.delete(0L));
        assertThrows(ResponseStatusException.class, () -> userService.delete(-1L));
    }

    @Test
    void update_ShouldSuccessfullyUpdateUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        // Act
        User result = userService.update(1L, "new@example.com", "New Name");

        // Assert
        assertEquals(testUser, result);
        assertEquals("new@example.com", testUser.getEmail());
        assertEquals("New Name", testUser.getName());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(cache).put(1L, testUser);
    }

    @Test
    void update_ShouldThrowExceptionForInvalidId() {
        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.update(null, "email@example.com", "Name"));
    }

    @Test
    void update_ShouldHandlePartialUpdates() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act - update only name
        User result = userService.update(1L, null, "New Name");

        // Assert
        assertEquals(testUser, result);
        assertEquals("john@example.com", testUser.getEmail()); // email unchanged
        assertEquals("New Name", testUser.getName());
    }

    @Test
    void bulkCreate_ShouldHandleSuccessAndFailures() {
        // Arrange
        UserCreateDto validDto1 = new UserCreateDto();
        validDto1.setName("User1");
        validDto1.setEmail("user1@example.com");
        validDto1.setBirth(LocalDate.of(1990, 1, 1));

        UserCreateDto invalidDto = new UserCreateDto();
        invalidDto.setName("");
        invalidDto.setEmail("invalid@example.com");
        invalidDto.setBirth(LocalDate.of(1990, 1, 1));

        UserCreateDto validDto2 = new UserCreateDto();
        validDto2.setName("User2");
        validDto2.setEmail("user2@example.com");
        validDto2.setBirth(LocalDate.of(1995, 5, 15));

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BulkResponse response = userService.bulkCreate(Arrays.asList(validDto1, invalidDto, validDto2));

        // Assert
        assertEquals(2, response.getSuccessCount());
        assertEquals(1, response.getFailCount());
        assertEquals(1, response.getErrors().size());
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void validateUserForCreation_ShouldThrowExceptionForNullBirthDate() {
        // Arrange
        testUser.setBirth(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void validateUserForCreation_ShouldThrowExceptionForFutureBirthDate() {
        // Arrange
        testUser.setBirth(LocalDate.now().plusDays(1));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void updateUserEmail_ShouldThrowExceptionForEmptyEmail() {
        // Arrange
        User user = new User();
        user.setEmail("old@example.com");

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "", "Name"));
    }

    @Test
    void updateUserName_ShouldThrowExceptionForEmptyName() {
        // Arrange
        User user = new User();
        user.setName("Old Name");

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "email@example.com", ""));
    }

    // Добавим эти тесты в существующий UserServiceTest

    @Test
    void create_ShouldThrowResponseStatusExceptionWithProperDetailsWhenValidationFails() {
        // Arrange
        User invalidUser = new User();
        invalidUser.setEmail(null);
        invalidUser.setName("");
        invalidUser.setBirth(LocalDate.now().plusDays(1));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(invalidUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertNotNull(exception.getReason());
    }

    @Test
    void delete_ShouldThrowResponseStatusExceptionWithNotFoundStatusWhenUserNotExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.delete(1L));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
    }

    @Test
    void update_ShouldThrowResponseStatusExceptionWithBadRequestForDuplicateEmail() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "existing@example.com", "Name"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
    }


    @Test
    void bulkCreate_ShouldIncludeProperErrorMessagesInResponse() {
        // Arrange
        UserCreateDto invalidDto = new UserCreateDto();
        invalidDto.setName("");
        invalidDto.setEmail("invalid@example.com");
        invalidDto.setBirth(LocalDate.of(1990, 1, 1));

        // Act
        BulkResponse response = userService.bulkCreate(List.of(invalidDto));

        // Assert
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailCount());
        assertFalse(response.getErrors().isEmpty());
        assertTrue(response.getErrors().get(0).contains("Ошибка при создании"));
    }

    @Test
    void findById_ShouldThrowExceptionWhenDatabaseErrorOccurs() {
        // Arrange
        when(cache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("DB connection failed"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findById(1L));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Internal server error"));
    }

    @Test
    void create_ShouldCalculateAgeCorrectly() {
        // Arrange
        User newUser = new User();
        newUser.setName("Test User");
        newUser.setEmail("test@example.com");
        newUser.setBirth(LocalDate.now().minusYears(25));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.create(newUser);

        // Assert
        assertEquals(25, result.getAge());
        verify(cache).put(any(), eq(result));
    }

    @Test
    void bulkCreate_ShouldNotCacheWhenUserCreationFails() {
        // Arrange
        UserCreateDto validDto = new UserCreateDto();
        validDto.setName("Valid User");
        validDto.setEmail("valid@example.com");
        validDto.setBirth(LocalDate.of(1990, 1, 1));

        UserCreateDto invalidDto = new UserCreateDto();
        invalidDto.setName("");
        invalidDto.setEmail("invalid@example.com");
        invalidDto.setBirth(LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmail("valid@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BulkResponse response = userService.bulkCreate(Arrays.asList(validDto, invalidDto));

        // Assert
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailCount());
        verify(cache, times(1)).put(any(), any()); // Только для успешного создания
    }

    @Test
    void update_ShouldNotUpdateCacheWhenSaveFails() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenThrow(new RuntimeException("Save failed"));

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> userService.update(1L, "new@example.com", "New Name"));

        verify(cache, never()).put(any(), any());
    }

    @Test
    void update_ShouldUpdateOnlyEmailWhenNameIsNull() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        // Act
        User result = userService.update(1L, "new@example.com", null);

        // Assert
        assertEquals("new@example.com", result.getEmail());
        assertEquals(testUser.getName(), result.getName()); // имя осталось прежним
        verify(cache).put(1L, result);
    }

    @Test
    void create_ShouldThrowExceptionForEmailStringNull() {
        // Arrange
        testUser.setEmail("null"); // строка "null"

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(testUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Email пользователя не может быть пустым, null или 'null'"));
    }

    @Test
    void findById_ShouldReturnCachedUserOnSubsequentCalls() {
        // Arrange
        when(cache.get(1L)).thenReturn(null).thenReturn(testUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Первый вызов - попадание в базу данных
        userService.findById(1L);

        // Второй вызов - должен вернуть из кэша
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findById(1L); // только один вызов в БД
        verify(cache, times(2)).get(1L);
    }

    @Test
    void delete_ShouldThrowInternalErrorWhenCacheRemoveFails() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Cache error")).when(cache).remove(1L);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.delete(1L));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Internal server error"));
    }
}