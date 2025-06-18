package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChooseLangCommandHandler implements BotCommandHandler {

    private final TelegramSender telegramSender;

    private final BotExceptionHandler botExceptionHandler;
    private final HelpCommandHandler helpCommandHandler;
    private final StartCommandHandler startCommandHandler;
    private final ValuationMetricsCommandHandler valuationMetricsCommandHandler;

    public ChooseLangCommandHandler(TelegramSender telegramSender, HelpCommandHandler helpCommandHandler, StartCommandHandler startCommandHandler,
                                    ValuationMetricsCommandHandler valuationMetricsCommandHandler, BotExceptionHandler botExceptionHandler) {
        this.telegramSender = telegramSender;
        this.helpCommandHandler = helpCommandHandler;
        this.startCommandHandler = startCommandHandler;
        this.valuationMetricsCommandHandler = valuationMetricsCommandHandler;
        this.botExceptionHandler = botExceptionHandler;
    }

    @Override
    public boolean canHandle(String command) {
        return "/set_lang".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        switch (arg) {
            case "EN":
                helpCommandHandler.setLocale(Locale.ENGLISH);
                startCommandHandler.setLocale(Locale.ENGLISH);
                valuationMetricsCommandHandler.setLocale(Locale.ENGLISH);
                botExceptionHandler.setLocale(Locale.ENGLISH);
                telegramSender.sendMessage(chatId, "Language was successfully changed");
                break;
            case "RU":
                helpCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                startCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                valuationMetricsCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                botExceptionHandler.setLocale(Locale.forLanguageTag("ru"));
                telegramSender.sendMessage(chatId, "Язык был успешно изменен");
                break;
            default:
                throw new IllegalCommandArgException("Передан некорректный аргумент команды /set_lang");
        }
    }
}
