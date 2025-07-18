package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Setter
@Component
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

        log.info("HelpCommandHandler started handling command");

        if (arg != null) {
            throw new IllegalCommandArgException("Command argument is not null");
        }

        telegramSender.sendMessage(chatId, messageService.generateHelpCommand(locale));
        log.info("HelpCommandHandler finished handling command successfully");
    }
}
