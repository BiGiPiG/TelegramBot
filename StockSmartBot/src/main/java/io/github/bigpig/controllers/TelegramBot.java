package io.github.bigpig.controllers;

import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.SmartAnalysisException;
import io.github.bigpig.handlers.BotExceptionHandler;
import io.github.bigpig.services.ChartService;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.services.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    ShareService shareService;
    ChartService chartService;
    MessageService messageService;
    BotExceptionHandler botExceptionHandler;

    public TelegramBot(@Value("${bot.token}")String botToken,
                       ShareService shareService, ChartService chartService, MessageService messageService, BotExceptionHandler botExceptionHandler) {
        super(botToken);
        this.shareService = shareService;
        this.chartService = chartService;
        this.messageService = messageService;
        this.botExceptionHandler = botExceptionHandler;
    }

    private static final String HELP_COMMAND = "/help";
    private static final String START_COMMAND = "/start";
    private static final String GET_VALUATION_METRICS_COMMAND = "/getValuationMetrics";
    private static final String GET_SMART_ANALYSE_COMMAND = "/getSmartAnalyse";
    private static final String GET_CHART = "/getChart";

    @Value("${bot.name}")
    private String botName;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        long id = user.getId();
        String[] message = msg.getText().split(" ");

        try {
            switch (message[0]) {
                case HELP_COMMAND:
                    helpCommand(id);
                    break;
                case START_COMMAND:
                    startCommand(id);
                    break;
                case GET_VALUATION_METRICS_COMMAND:
                    if (message.length == 2) {
                        getValuationMetrics(id, message[1]);
                    }
                    break;
                case GET_SMART_ANALYSE_COMMAND:
                    if (message.length == 2) {
                        getAsyncSmartAnalyse(id, message[1]);
                    }
                    break;
                case GET_CHART:
                    if (message.length == 2) {
                        getChart(id, message[1]);
                    }
                    break;
                default:
                    System.out.println("Unknown command: " + message[0]);
            }
        } catch (Exception e) {
            String errorMsg = messageService.generateErrorMessage(botExceptionHandler.handleException(e));
            sendMessage(id, errorMsg);
        }
    }

    public void helpCommand(long chatId) {
        String cmdText = messageService.generateHelpCommand();
        sendMessage(chatId, cmdText);
    }

    public void startCommand(long chatId) {
        String cmdText = messageService.generateStartCommand();
        sendMessage(chatId, cmdText);
    }

    public void getValuationMetrics(long chatId, String ticker) {
        ShareDTO curShare;
        curShare = shareService.calculateValuationMetrics(ticker);

        String cmdText = messageService.generateGetValuationMetricsCommand(curShare);

        sendMessage(chatId, cmdText);
    }

    public void getAsyncSmartAnalyse(long chatId, String ticker) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        int messageId = sendMessage(chatId, messageService.generateProcessingMessage(0));

        AtomicInteger dotCount = new AtomicInteger(1);

        executor.scheduleAtFixedRate(() -> {
            int dots = dotCount.getAndIncrement() % 4; // 0 to 3

            String updatedText = messageService.generateProcessingMessage(dots);
            try {
                editMessage(chatId, messageId, updatedText);
            } catch (TelegramApiException e) {
                String errorMessage = botExceptionHandler.handleException(e);
                sendMessage(chatId, messageService.generateErrorMessage(errorMessage));
            }
        }, 1, 1, TimeUnit.SECONDS);

        shareService.getSmartAnalyse(ticker).thenAccept(result -> {
            executor.shutdownNow();

            try {
                deleteMessage(chatId, messageId);
            } catch (TelegramApiException e) {
                String errorMessage = botExceptionHandler.handleException(e);
                sendMessage(chatId, messageService.generateErrorMessage(errorMessage));
                return;
            }

            sendMessage(chatId, messageService.generateSmartAnalyseResult(result));
        }).exceptionally(ex -> {
            executor.shutdownNow();

            try {
                deleteMessage(chatId, messageId);
            } catch (TelegramApiException e) {
                String errorMessage = botExceptionHandler.handleException(e);
                sendMessage(chatId, messageService.generateErrorMessage(errorMessage));
            }

            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            SmartAnalysisException analysisException;

            if (cause instanceof SmartAnalysisException) {
                analysisException = (SmartAnalysisException) cause;
            } else {
                analysisException = new SmartAnalysisException("Ошибка анализа", cause);
            }

            String errorMessage = botExceptionHandler.handleException(analysisException);
            sendMessage(chatId, messageService.generateErrorMessage(errorMessage));

            return null;
        });
    }

    private int sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        try {
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void editMessage(long chatId, int messageId, String text) throws TelegramApiException {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        execute(editMessage);
    }

    public void deleteMessage(long chatId, int messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .build();

        execute(deleteMessage);
    }

    public void getChart(long chatId, String ticker) throws TelegramApiException, IOException {
        chartService.generateChart(ticker);
        Path chartPath = Paths.get("tmpDir", ticker + ".png");

        if (!Files.exists(chartPath)) {
            throw new TelegramApiException("Chart file not found at: " + chartPath);
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(chartPath.toFile()));
        execute(photo);

        Files.deleteIfExists(chartPath);
    }
}
