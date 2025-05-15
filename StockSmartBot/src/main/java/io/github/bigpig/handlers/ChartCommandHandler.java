package io.github.bigpig.handlers;

import io.github.bigpig.services.ChartService;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.services.ShareService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ChartCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private final ChartService chartService;

    public ChartCommandHandler(MessageService messageService, TelegramSender telegramSender,
                               ChartService chartService) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.chartService = chartService;
    }

    @Override
    public boolean canHandle(String command) {
        return "/getChart".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {
        Path chartPath = Paths.get("tmpDir", arg + ".png");
        try {
            chartService.generateChart(arg);

            if (!Files.exists(chartPath)) {
                throw new TelegramApiException("Файл графика не найден по пути: " + chartPath);
            }

            telegramSender.sendPhoto(chatId, chartPath);

        } catch (IOException | TelegramApiException e) {
            telegramSender.sendMessage(chatId, messageService.generateErrorMessage("Ошибка при отправке графика: " + e.getMessage()));
        } finally {
            try {
                Files.deleteIfExists(chartPath);
            } catch (IOException e) {
                System.err.println("Не удалось удалить временный файл: " + e.getMessage());
            }
        }
    }
}
