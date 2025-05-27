package com.kirill.meetyou.controller;

import com.kirill.meetyou.dto.BulkResponse;
import com.kirill.meetyou.dto.UserCreateDto;
import com.kirill.meetyou.dto.UserUpdateDto;
import com.kirill.meetyou.model.User;
import com.kirill.meetyou.repository.UserRepository;
import com.kirill.meetyou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей системы")
    @ApiResponse(responseCode = "200",
            description = "Список пользователей успешно получен")
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Пользователь успешно найден"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Юзер с id: " + id + " не найден")));
    }

    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя в системе")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
    @PostMapping(consumes = "application/json")
    public ResponseEntity<User> create(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
    }

    @Operation(summary = "Массовое создание пользователей",
            description = "Создает несколько пользователей одновременно")
    @ApiResponse(responseCode = "201", description = "Пользователи успешно созданы")
    @PostMapping("/bulk")
    public BulkResponse bulkCreate(@RequestBody List<UserCreateDto> userDtos) {
        return userService.bulkCreate(userDtos);
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по его идентификатору")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет информацию о пользователе (email и/или имя)")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен")
    @PutMapping(path = "{id}", consumes = "application/json")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody UserUpdateDto dto
    ) {
        userService.update(id, dto);
        return ResponseEntity.ok().build();
    }


    // Обновленные методы для работы с интересами (теперь через String)
    @Operation(summary = "Поиск по интересу",
            description = "Возвращает пользователей, у которых есть указанный интерес")
    @ApiResponse(responseCode = "200",
            description = "Список пользователей успешно получен")
    @GetMapping("/by-interest")
    public ResponseEntity<List<User>> getUsersByInterest(
            @RequestParam String interestType) {
        return ResponseEntity.ok(userRepository.findUsersByInterestType(interestType));
    }

    @Operation(summary = "Поиск по всем интересам",
            description = "Возвращает пользователей, у которых есть все указанные интересы")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
        @ApiResponse(responseCode = "400", description = "Не указаны интересы для поиска")
    })
    @GetMapping("/by-all-interests")
    public ResponseEntity<List<User>> getUsersByAllInterests(
            @RequestParam(required = false) Set<String> interestTypes) {
        if (interestTypes == null || interestTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не указаны интересы для поиска");
        }
        return ResponseEntity.ok(userRepository.findUsersByAllInterestTypes(interestTypes,
                interestTypes.size()));
    }

    @Operation(summary = "Поиск по любому из интересов",
            description = "Возвращает пользователей, у которых"
                    + " есть хотя бы один из указанных интересов")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
        @ApiResponse(responseCode = "400", description = "Не указаны интересы для поиска")
    })
    @GetMapping("/by-any-interest")
    public ResponseEntity<List<User>> getUsersByAnyInterest(
            @RequestParam Set<String> interestTypes) {
        if (interestTypes == null || interestTypes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Не указаны интересы для поиска");
        }
        return ResponseEntity.ok(userRepository.findUsersByAnyInterestTypes(interestTypes));
    }
}