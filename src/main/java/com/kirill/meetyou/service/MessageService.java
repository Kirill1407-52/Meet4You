package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.MessageDto;
import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Message;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.MessageRepository;
import com.kirill.meetyou.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    // Error message constants
    private static final String USER_NOT_FOUND_LOG = "❌ Пользователь не найден: ID {}";
    private static final String USER_NOT_FOUND_MSG = "Пользователь не найден";
    private static final String INTERLOCUTOR_NOT_FOUND_LOG = "❌ Собеседник не найден: ID {}";
    private static final String INTERLOCUTOR_NOT_FOUND_MSG = "Собеседник не найден";

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public MessageDto sendMessage(Long senderId, Long receiverId, String content) {
        log.info("🔄 Попытка отправки сообщения от {} к {}", senderId, receiverId);
        log.debug("Содержимое сообщения: {}", content);

        if (content == null || content.trim().isEmpty()) {
            log.error("❌ Отклонено: пустое содержание сообщения");
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, senderId);
                    return new ResourceNotFoundException(USER_NOT_FOUND_MSG);
                });

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> {
                    log.error(INTERLOCUTOR_NOT_FOUND_LOG, receiverId);
                    return new ResourceNotFoundException(INTERLOCUTOR_NOT_FOUND_MSG);
                });

        if (senderId.equals(receiverId)) {
            log.warn("⚠️ Попытка самосообщения: {}", senderId);
            throw new IllegalArgumentException("Нельзя отправить сообщение самому себе");
        }

        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        log.info("✅ Сообщение #{} успешно отправлено от {} к {} в {}",
                savedMessage.getId(),
                senderId,
                receiverId,
                savedMessage.getTimestamp().format(TIMESTAMP_FORMATTER));
        log.debug("Полные данные сообщения: {}", savedMessage);

        return convertToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(Long user1Id, Long user2Id) {
        log.info("📖 Запрос переписки между {} и {}", user1Id, user2Id);

        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, user1Id);
                    return new ResourceNotFoundException(USER_NOT_FOUND_MSG);
                });

        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, user2Id);
                    return new ResourceNotFoundException(USER_NOT_FOUND_MSG);
                });

        List<MessageDto> conversation = messageRepository.findConversation(user1, user2).stream()
                .map(this::convertToDto)
                .toList();

        log.info("📊 Найдено {} сообщений в переписке", conversation.size());
        log.debug("Первые 3 сообщения: {}",
                conversation.stream().limit(3).toList());

        return conversation;
    }

    @Transactional
    public void markMessagesAsRead(Long userId, Long interlocutorId) {
        log.info("👁️ Пользователь {} помечает сообщения от {} как прочитанные",
                userId, interlocutorId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, userId);
                    return new ResourceNotFoundException(USER_NOT_FOUND_MSG);
                });

        User interlocutor = userRepository.findById(interlocutorId)
                .orElseThrow(() -> {
                    log.error(INTERLOCUTOR_NOT_FOUND_LOG, interlocutorId);
                    return new ResourceNotFoundException(INTERLOCUTOR_NOT_FOUND_MSG);
                });

        List<Message> unreadMessages =
                messageRepository.findByReceiverAndSenderAndIsReadFalse(user, interlocutor);

        log.info("📌 Найдено {} непрочитанных сообщений", unreadMessages.size());

        unreadMessages.forEach(message -> {
            message.setRead(true);
            log.debug("Сообщение #{} помечено как прочитанное", message.getId());
        });

        messageRepository.saveAll(unreadMessages);
        log.info("✅ Все сообщения от {} помечены как прочитанные", interlocutorId);
    }

    @Transactional(readOnly = true)
    public long getUnreadMessagesCount(Long userId) {
        log.debug("🔍 Запрос количества непрочитанных сообщений для {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_LOG, userId);
                    return new ResourceNotFoundException(USER_NOT_FOUND_MSG);
                });

        long count = messageRepository.countByReceiverAndIsReadFalse(user);

        log.info("📊 Пользователь {} имеет {} непрочитанных сообщений", userId, count);
        return count;
    }

    public MessageDto convertToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .senderId(message.getSender().getId())
                .receiverId(message.getReceiver().getId())
                .isRead(message.isRead())
                .build();
    }
}