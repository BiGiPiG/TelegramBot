package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.exceptions.SmartAnalysisException;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.services.ShareService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AsyncSmartAnalyseCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private final ShareService shareService;

    public AsyncSmartAnalyseCommandHandler(MessageService messageService, TelegramSender telegramSender,
                                          ShareService shareService) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.shareService = shareService;
    }

    @Override
    public boolean canHandle(String command) {
        return "/ai_analysis".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger dotCount = new AtomicInteger(1);

        int messageId = telegramSender.sendMessage(chatId, messageService.generateProcessingMessage(0));

        executor.scheduleAtFixedRate(() -> {
            try {
                telegramSender.editMessage(
                        chatId, messageId,
                        messageService.generateProcessingMessage(dotCount.getAndIncrement() % 4)
                );
            } catch (TelegramApiException e) {
                telegramSender.sendMessage(chatId, "Ошибка обновления статуса.");
            }
        }, 1, 1, TimeUnit.SECONDS);

        shareService.getSmartAnalyse(arg)
            .thenAccept(result -> {
            finishProgressAnimation(executor, chatId, messageId);
            telegramSender.sendMessage(chatId, messageService.generateSmartAnalyseResult(result));
        }).exceptionally(ex -> {
            handleSmartAnalyseError(executor, chatId, messageId, ex);
            return null;
        });
    }

    private void handleSmartAnalyseError(ScheduledExecutorService executor, long chatId, int messageId, Throwable ex) {
        finishProgressAnimation(executor, chatId, messageId);

        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;

        if (cause instanceof SmartAnalysisException) {
            throw (SmartAnalysisException) cause;
        } else {
            throw new SmartAnalysisException("Произошла неизвестная ошибка при анализе.");
        }
    }

    private void finishProgressAnimation(ScheduledExecutorService executor, long chatId, int messageId) {
        executor.shutdownNow();
        try {
            telegramSender.deleteMessage(chatId, messageId);
        } catch (TelegramApiException e) {
            telegramSender.sendMessage(chatId, "Не удалось удалить сообщение прогресса.");
        }
    }
}
