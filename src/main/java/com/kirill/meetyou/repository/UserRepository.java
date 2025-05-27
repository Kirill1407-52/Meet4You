package com.kirill.meetyou.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.kirill.meetyou.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "select * from users where email = :email", nativeQuery = true)
    Optional<User> findByEmail(String email);

    // Поиск пользователей по названию интереса
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE i.interestType = :interestType")
    List<User> findUsersByInterestType(@Param("interestType") String interestType);

    @Query("SELECT u FROM User u JOIN u.interests i WHERE i.interestType IN "
            + ":interestTypes GROUP BY u HAVING COUNT(DISTINCT i) = :interestCount")
    List<User> findUsersByAllInterestTypes(
            @Param("interestTypes") Set<String> interestTypes,
            @Param("interestCount") long interestCount);

    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i "
            + "WHERE i.interestType IN :interestTypes")
    List<User> findUsersByAnyInterestTypes(
            @Param("interestTypes") Set<String> interestTypes);
}