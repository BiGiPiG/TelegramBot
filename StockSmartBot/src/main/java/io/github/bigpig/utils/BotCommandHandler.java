package io.github.bigpig.utils;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public interface BotCommandHandler {
    boolean canHandle(String command);
    void handle(long chatId, String arg) throws IOException, TelegramApiException;
}
