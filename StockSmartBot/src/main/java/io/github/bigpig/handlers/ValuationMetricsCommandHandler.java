package io.github.bigpig.handlers;

import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.IllegalCommandArgException;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.services.ShareService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Setter
@Component
public class ValuationMetricsCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private final ShareService shareService;
    private Locale locale;

    public ValuationMetricsCommandHandler(MessageService messageService, TelegramSender telegramSender,
                                          ShareService shareService) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.shareService = shareService;
        this.locale = Locale.ENGLISH;
    }

    @Override
    public boolean canHandle(String command) {
        return "/metrics".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {

        log.info("ValuationMetricsCommandHandler started handling command");

        if (arg == null) {
            throw new IllegalCommandArgException("Command argument is null");
        }

        ShareDTO curShare = shareService.calculateValuationMetrics(arg);
        String cmdText = messageService.generateGetValuationMetricsCommand(curShare, locale);
        telegramSender.sendMessage(chatId, cmdText);

        log.info("ValuationMetricsCommandHandler finished handling command successfully");
    }
}
