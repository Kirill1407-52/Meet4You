package com.kirill.meetyou.repository;

import java.util.List;

import com.kirill.meetyou.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN u.friends f WHERE f.id = :userId")
    List<User> findFriendsOfUser(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM User u "
            + "JOIN u.friends f WHERE u.id = :userId AND f.id = :friendId")
    boolean existsFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
