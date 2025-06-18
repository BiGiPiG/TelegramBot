package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.utils.BotCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class ChooseLangCommandHandler implements BotCommandHandler {

    private final BotExceptionHandler botExceptionHandler;
    private final HelpCommandHandler helpCommandHandler;
    private final StartCommandHandler startCommandHandler;
    private final ValuationMetricsCommandHandler valuationMetricsCommandHandler;

    public ChooseLangCommandHandler(HelpCommandHandler helpCommandHandler, StartCommandHandler startCommandHandler,
                                    ValuationMetricsCommandHandler valuationMetricsCommandHandler, BotExceptionHandler botExceptionHandler) {
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

        log.info("ChooseLangCommandHandler started handling command with argument: {}", arg);

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        Locale locale;
        switch (arg) {
            case "EN":
                locale = Locale.forLanguageTag("en");
                setLocaleForAllHandlers(locale);
                log.info("Language changed to English for chat {}", chatId);
                break;
            case "RU":
                locale = Locale.forLanguageTag("ru");
                setLocaleForAllHandlers(locale);
                log.info("Language changed to Russian for chat {}", chatId);
                break;
            default:
                throw new IllegalCommandArgException("Unsupported language: " + arg);
        }
        log.info("ChooseLangCommandHandler finished successfully handling command with argument: {}", arg);
    }

    private void setLocaleForAllHandlers(Locale locale) {
        helpCommandHandler.setLocale(locale);
        startCommandHandler.setLocale(locale);
        valuationMetricsCommandHandler.setLocale(locale);
        botExceptionHandler.setLocale(locale);
    }
}
