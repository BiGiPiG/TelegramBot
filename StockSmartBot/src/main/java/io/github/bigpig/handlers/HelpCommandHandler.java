package io.github.bigpig.handlers;

import io.github.bigpig.services.MessageService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Setter
public class HelpCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private Locale locale;

    public HelpCommandHandler(MessageService messageService, TelegramSender telegramSender) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.locale = Locale.ENGLISH;
    }

    @Override
    public boolean canHandle(String command) {
        return "/help".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {
        telegramSender.sendMessage(chatId, messageService.generateHelpCommand(locale));
    }
}
