package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.services.ChartService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ChartCommandHandler implements BotCommandHandler {

    private final TelegramSender telegramSender;
    private final ChartService chartService;

    public ChartCommandHandler(TelegramSender telegramSender,
                               ChartService chartService) {
        this.telegramSender = telegramSender;
        this.chartService = chartService;
    }

    @Override
    public boolean canHandle(String command) {
        return "/chart".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) throws IOException, TelegramApiException {

        log.info("ChartCommandHandler started handling command with argument: {}", arg);

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        Path chartPath = Paths.get("tmpDir", arg + ".png");
        try {
            chartService.generateChart(arg);

            if (!Files.exists(chartPath)) {
                throw new TelegramApiException("Файл графика не найден по пути: " + chartPath);
            }
            telegramSender.sendPhoto(chatId, chartPath);
        } finally {
            Files.deleteIfExists(chartPath);
            log.info("ChartCommandHandler finished handling command with argument: {}", arg);
        }
    }
}
