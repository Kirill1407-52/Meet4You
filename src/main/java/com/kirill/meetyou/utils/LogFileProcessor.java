package com.kirill.meetyou.utils;

import static java.lang.Thread.sleep;

import com.kirill.meetyou.dto.LogTask;
import com.kirill.meetyou.enums.LogTaskStatus;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogFileProcessor {
    @Async
    public void processRange(String fromDate, String toDate, String taskId, Map<String, LogTask> tasks) {
        LogTask task = tasks.get(taskId);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate from = LocalDate.parse(fromDate, formatter);
            LocalDate to = LocalDate.parse(toDate, formatter);

            if (from.isAfter(to)) {
                throw new IllegalArgumentException("Неверный диапазон дат");
            }

            List<Path> logFiles = new ArrayList<>();
            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                Path path = Paths.get("logs/meetyou-app-" + date.format(formatter) + ".log");
                if (Files.exists(path)) {
                    logFiles.add(path);
                }
            }
            sleep(50000);
            if (logFiles.isEmpty()) {
                throw new NotFoundException("Логи за период не найдены");
            }

            Path mergedPath = Paths.get("logs/generated-log-" + taskId + ".log");
            mergeLogFiles(mergedPath, logFiles);

            task.setStatus(LogTaskStatus.SUCCESS);
            task.setFilePath(mergedPath.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(LogTaskStatus.FAILED);
            task.setError(e.getMessage());
            log.error("[ASYNC] Ошибка генерации логов по диапазону: {}", e.getMessage());
        }
    }

    private void mergeLogFiles(Path mergedPath, List<Path> logFiles) throws InterruptedException {
        try (BufferedWriter writer = Files.newBufferedWriter(mergedPath)) {
            for (Path path : logFiles) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new InterruptedException();
        }
    }
}