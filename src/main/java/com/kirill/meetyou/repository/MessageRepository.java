package com.kirill.meetyou.repository;

import com.kirill.meetyou.model.Message;
import com.kirill.meetyou.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Для получения переписки между двумя пользователями
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) "
            + "OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    // Для поиска непрочитанных сообщений от конкретного отправителя
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver "
            + "AND m.sender = :sender AND m.isRead = false")
    List<Message> findByReceiverAndSenderAndIsReadFalse(@Param("rece"
            + "iver") User receiver, @Param("sender") User sender);

    // Для подсчета непрочитанных сообщений
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.isRead = false")
    long countByReceiverAndIsReadFalse(@Param("receiver") User receiver);
}