package io.github.bigpig.utils;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.nio.file.Path;
import java.io.IOException;

public interface TelegramOperations {
    int sendMessage(long chatId, String text);
    void editMessage(long chatId, int messageId, String text) throws TelegramApiException;
    void deleteMessage(long chatId, int messageId) throws TelegramApiException;
    void sendPhoto(long chatId, Path chartPath) throws TelegramApiException, IOException;
}
