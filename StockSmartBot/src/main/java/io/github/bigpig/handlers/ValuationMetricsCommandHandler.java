package io.github.bigpig.handlers;

import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.services.MessageService;
import io.github.bigpig.services.ShareService;
import io.github.bigpig.utils.BotCommandHandler;
import io.github.bigpig.utils.TelegramSender;
import org.springframework.stereotype.Component;

@Component
public class ValuationMetricsCommandHandler implements BotCommandHandler {

    private final MessageService messageService;
    private final TelegramSender telegramSender;
    private final ShareService shareService;

    public ValuationMetricsCommandHandler(MessageService messageService, TelegramSender telegramSender,
                                          ShareService shareService) {
        this.messageService = messageService;
        this.telegramSender = telegramSender;
        this.shareService = shareService;
    }

    @Override
    public boolean canHandle(String command) {
        return "/getValuationMetrics".equals(command);
    }

    @Override
    public void handle(long chatId, String arg) {
        ShareDTO curShare = shareService.calculateValuationMetrics(arg);
        String cmdText = messageService.generateGetValuationMetricsCommand(curShare);
        telegramSender.sendMessage(chatId, cmdText);
    }
}
