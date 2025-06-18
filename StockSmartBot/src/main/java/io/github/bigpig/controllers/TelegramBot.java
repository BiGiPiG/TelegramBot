package io.github.bigpig.controllers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.handlers.BotExceptionHandler;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final MessageService messageService;
    private final BotExceptionHandler botExceptionHandler;
    private final TelegramSender telegramSender;
    private final List<BotCommandHandler> commandHandlers;

    public TelegramBot(@Value("${bot.token}")String botToken,
                       MessageService messageService,
                       BotExceptionHandler botExceptionHandler, TelegramSender telegramSender,
                       List<BotCommandHandler> commandHandlers) {
        super(botToken);
        this.messageService = messageService;
        this.botExceptionHandler = botExceptionHandler;
        this.telegramSender = telegramSender;
        this.commandHandlers = commandHandlers;

    }

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
        String command = message[0];
        String arg = message.length > 1 ? message[1] : null;

        try {
            log.info("Received command {} from chat {} with arg: {}",
                    command, id, arg);
            commandHandlers.stream()
                    .filter(h -> h.canHandle(command))
                    .findFirst()
                    .orElseThrow(() -> new IllegalCommandArgException("Unknown command: " + command))
                    .handle(id, arg);
            log.info("Command {} processed successfully for chat {}", command, id);
        } catch (Exception e) {
            log.error("Error processing command {} for chat {}:{}",
                    command, id, e.getMessage());

            String errorMsg = messageService.generateErrorMessage(botExceptionHandler.handleException(e));
            telegramSender.sendMessage(id, errorMsg);
        }
    }
}
