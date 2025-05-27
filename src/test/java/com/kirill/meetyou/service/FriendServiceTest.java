package com.kirill.meetyou.service;

import com.kirill.meetyou.repository.FriendRepository;
import com.kirill.meetyou.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private FriendService friendService;

    private final Long userId = 1L;
    private final Long friendId = 2L;

    @Test
    void addFriend_Success() {
        // Arrange
        User user = new User();
        User friend = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(false);

        // Act
        friendService.addFriend(userId, friendId);

        // Assert
        verify(userRepository).save(user);
        assertTrue(user.getFriends().contains(friend));
    }

    @Test
    void addFriend_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                friendService.addFriend(userId, friendId));
    }

    @Test
    void addFriend_FriendNotFound_ThrowsException() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                friendService.addFriend(userId, friendId));
    }

    @Test
    void addFriend_SelfFriendship_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                friendService.addFriend(userId, userId));
    }

    @Test
    void addFriend_AlreadyFriends_ThrowsException() {
        User user = new User();
        User friend = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                friendService.addFriend(userId, friendId));
    }

    @Test
    void removeFriend_Success() {
        User user = new User();
        User friend = new User();
        user.addFriend(friend);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(true);

        friendService.removeFriend(userId, friendId);

        verify(userRepository).save(user);
        assertFalse(user.getFriends().contains(friend));
    }

    @Test
    void removeFriend_NotFriends_ThrowsException() {
        User user = new User();
        User friend = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                friendService.removeFriend(userId, friendId));
    }

    @Test
    void getAllFriends_Success() {
        User friend = new User();
        when(friendRepository.findFriendsOfUser(userId)).thenReturn(List.of(friend));

        List<User> friends = friendService.getAllFriends(userId);

        assertEquals(1, friends.size());
        assertEquals(friend, friends.get(0));
    }

    @Test
    void getAllFriends_NoFriends_ReturnsEmptyList() {
        when(friendRepository.findFriendsOfUser(userId)).thenReturn(Collections.emptyList());

        List<User> friends = friendService.getAllFriends(userId);

        assertTrue(friends.isEmpty());
    }

    @Test
    void checkFriendship_Exists_ReturnsTrue() {
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(true);

        boolean result = friendService.checkFriendship(userId, friendId);

        assertTrue(result);
    }

    @Test
    void checkFriendship_NotExists_ReturnsFalse() {
        when(friendRepository.existsFriendship(userId, friendId)).thenReturn(false);

        boolean result = friendService.checkFriendship(userId, friendId);

        assertFalse(result);
    }

    // Дополнительные тесты для улучшения покрытия
    @Test
    void removeFriend_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                friendService.removeFriend(userId, friendId));
    }

    @Test
    void removeFriend_FriendNotFound_ThrowsException() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(friendId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                friendService.removeFriend(userId, friendId));
    }
}