package io.github.bigpig.handlers;

import io.github.bigpig.services.MessageService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;

    public StartCommandHandler(MessageService messageService, TelegramSender telegramSender) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
    }

    @Override
    public boolean canHandle(String command) {
        return "/start".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {
        telegramSender.sendMessage(chatId, messageService.generateStartCommand());
    }
}
