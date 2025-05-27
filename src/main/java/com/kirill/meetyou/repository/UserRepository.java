package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmail(String email);

    // Поиск пользователей по названию интереса (без учета регистра)
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE"
            + " LOWER(i.interestType) = LOWER(:interestType)")
    List<User> findUsersByInterestType(@Param("interestType") String interestType);

    // Поиск пользователей, у которых есть ВСЕ указанные интересы (без учета регистра)
    @Query("SELECT u FROM User u JOIN u.interests i WHERE LOWER(i.interestType) IN "
            + "(SELECT LOWER(it) FROM Interest it WHERE it IN :interestTypes) "
            + "GROUP BY u HAVING COUNT(DISTINCT i) = :interestCount")
    List<User> findUsersByAllInterestTypes(
            @Param("interestTypes") Set<String> interestTypes,
            @Param("interestCount") long interestCount);

    // Поиск пользователей, у которых есть ЛЮБОЙ из указанных интересов (без учета регистра)
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i "
            + "WHERE LOWER(i.interestType) IN (SELECT LOWER(it) "
            + "FROM Interest it WHERE it IN :interestTypes)")
    List<User> findUsersByAnyInterestTypes(
            @Param("interestTypes") Set<String> interestTypes);
}