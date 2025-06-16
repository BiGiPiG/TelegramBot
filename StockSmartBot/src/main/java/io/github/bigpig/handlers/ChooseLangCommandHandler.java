package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.utils.BotCommandHandler;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ChooseLangCommandHandler implements BotCommandHandler {

    HelpCommandHandler helpCommandHandler;
    StartCommandHandler startCommandHandler;
    ValuationMetricsCommandHandler valuationMetricsCommandHandler;

    public ChooseLangCommandHandler(HelpCommandHandler helpCommandHandler, StartCommandHandler startCommandHandler,
                                    ValuationMetricsCommandHandler valuationMetricsCommandHandler) {
        this.helpCommandHandler = helpCommandHandler;
        this.startCommandHandler = startCommandHandler;
        this.valuationMetricsCommandHandler = valuationMetricsCommandHandler;
    }

    @Override
    public boolean canHandle(String command) {
        return "/set_lang".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {
        switch (arg) {
            case "EN":
                helpCommandHandler.setLocale(Locale.ENGLISH);
                startCommandHandler.setLocale(Locale.ENGLISH);
                valuationMetricsCommandHandler.setLocale(Locale.ENGLISH);
                break;
            case "RU":
                helpCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                startCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                valuationMetricsCommandHandler.setLocale(Locale.forLanguageTag("ru"));
                break;
            default:
                throw new IllegalCommandArgException("Передан некорректный аргумент команды /set_lang");
        }
    }
}
