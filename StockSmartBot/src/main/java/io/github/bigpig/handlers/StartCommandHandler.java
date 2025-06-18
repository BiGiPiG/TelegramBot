package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Setter
public class StartCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private Locale locale;

    public StartCommandHandler(MessageService messageService, TelegramSender telegramSender) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.locale = Locale.ENGLISH;
    }

    @Override
    public boolean canHandle(String command) {
        return "/start".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        telegramSender.sendMessage(chatId, messageService.generateStartCommand(locale));
    }
}
