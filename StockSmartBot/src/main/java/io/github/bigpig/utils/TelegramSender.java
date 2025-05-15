package io.github.bigpig.utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.file.Path;

@Component
public class TelegramSender {

    private final ObjectProvider<TelegramLongPollingBot> botProvider;

    public TelegramSender(ObjectProvider<TelegramLongPollingBot> botProvider) {
        this.botProvider = botProvider;
    }

    public int sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        try {
            return getBot().execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void editMessage(long chatId, int messageId, String text) throws TelegramApiException {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        getBot().execute(editMessage);
    }

    public void deleteMessage(long chatId, int messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .build();

        getBot().execute(deleteMessage);
    }

    public void sendPhoto(long chatId, Path chartPath) throws TelegramApiException {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(chartPath.toFile()));
        getBot().execute(photo);
    }

    private TelegramLongPollingBot getBot() {
        return botProvider.getIfAvailable();
    }

}
