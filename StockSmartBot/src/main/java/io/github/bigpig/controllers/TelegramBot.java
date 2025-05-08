package io.github.bigpig.controllers;

import io.github.bigpig.dto.ShareDTO;
import io.github.bigpig.exceptions.CalculateValuationException;
import io.github.bigpig.exceptions.ExternalApiException;
import io.github.bigpig.exceptions.ShareNotFoundException;
import io.github.bigpig.services.ChartService;
import io.github.bigpig.services.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    ShareService shareService;
    ChartService chartService;

    public TelegramBot(@Value("${bot.token}")String botToken,
                       ShareService shareService, ChartService chartService) {
        super(botToken);
        this.shareService = shareService;
        this.chartService = chartService;
    }

    private static final String HELP_COMMAND = "/help";
    private static final String START_COMMAND = "/start";
    private static final String GET_VALUATION_METRICS_COMMAND = "/getValuationMetrics";
    private static final String GET_SMART_ANALYSE_COMMAND = "/getSmartAnalyse";
    private static final String GET_CHART = "/getChart";

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

        try {
            switch (message[0]) {
                case HELP_COMMAND:
                    helpCommand(id);
                    break;
                case START_COMMAND:
                    startCommand(id);
                    break;
                case GET_VALUATION_METRICS_COMMAND:
                    if (message.length == 2) {
                        getValuationMetrics(id, message[1]);
                    }
                    break;
                case GET_SMART_ANALYSE_COMMAND:
                    if (message.length == 2) {
                        getAsyncSmartAnalyse(id, message[1]);
                    }
                    break;
                case GET_CHART:
                    if (message.length == 2) {
                        getChart(id, message[1]);
                    }
                    break;
                default:
                    System.out.println("Unknown command: " + message[0]);
            }
        } catch (ShareNotFoundException e) {
            log.warn("Тикер не найден: {}", e.getMessage());
            sendMessage(id, escapeMarkdownSymbols("❌ Не удалось найти информацию по тикеру. Попробуйте другой."));
        } catch (ExternalApiException e) {
            log.error("Ошибка обращения к внешнему API: {}", e.getMessage(), e);
            sendMessage(id, escapeMarkdownSymbols("⚠️ Временная ошибка при получении данных. Пожалуйста, повторите позже."));
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            sendMessage(id, escapeMarkdownSymbols("❗ Произошла ошибка. Попробуйте снова позже."));
        }
    }

    public void helpCommand(long chatId) {
        String helpText = """
        📊 *Bot Commands Help* 📊
        
        *MarketInsightBot* -Your Market Analysis Assistant:
        - 📈 Stocks, ETFs and Cryptocurrencies
        - 🤖 AI-аналитика и прогнозирование
        - 🔍 Ключевые финансовые метрики
        
        🔹 *Основные команды*
        /start - Начать работу
        /help - Справка по командам
        
        📈 *Анализ активов*
        /getValuationMetrics [тикер] - Основные метрики (P/E, P/S и др.)
        
        🤖 *AI-аналитика*
        /getSmartAnalyse [тикер] - Анализ с искусственным интеллектом
        
        🖼 *Визуализация данных*
        /getChart [тикер]
        
        📌 *Примеры запросов*
        `/getValuationMetrics AAPL` - анализ Apple
        `/getSmartAnalyse AAPL` - AI-разбор Apple
        `/getChart AAPL` - график для Apple
        """;

        helpText = escapeMarkdownSymbols(helpText);
        SendMessage message = SendMessage.builder()
                .chatId(Long.toString(chatId))
                .text(helpText)
                .parseMode("MarkdownV2")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void startCommand(long chatId) {
        String startText = """
        🚀 *Добро пожаловать в MarketInsightBot!* 🚀

        *Ваш персональный помощник для анализа рынков:*
        - 📊 Ключевые метрики (P/E, P/S и др.)
        - 🤖 AI-анализ
        - 📈 Визуализация данных

        🔍 *Как начать?*
        Просто введите команду с тикером интересующего актива:

        `/getValuationMetrics MSFT` - анализ Microsoft
        `/getSmartAnalyse MSFT` - AI-разбор Microsoft

        📌 Для всех команд напишите /help
        """;

        startText = escapeMarkdownSymbols(startText);
        sendMessage(chatId, startText);
    }

    public void getValuationMetrics(long chatId, String ticker) {
        ShareDTO curShare;
        try {
            curShare = shareService.calculateValuationMetrics(ticker);
        } catch (CalculateValuationException e) {
            sendMessage(chatId, escapeMarkdownSymbols("⚠️ Не удалось получить информацию по тикеру"));
            return;
        }

        String ansText = escapeMarkdownSymbols(String.format("""
                    📈 *%s*:
                  
                    Price: `%s`
             
                    *Description*
                 
                    %s
                 
                    💹 *Daily Price Metrics*
                  
                    ▫️ High: `%s`
                    ▫️ Low: `%s`
                    ▫️ Change: `%s (%s%%)`
                    ▫️ Volume: `%s`

                    📊 *Fundamental Metrics*

                    P/E (Price/Earn): %s
                    P/B (Price/Book): %s
                    P/S (Price/Sale): %s
                    Market Capitalisation: %s
                    EPS: %s
                    Book Value: %s
                  """,
                String.format("%s", curShare.getName()),
                String.format("%s", curShare.getGlobalQuote().getCurrentPrice()),
                String.format("%s", curShare.getDescription().replace("'", "").replace("`", "")),
                String.format("%s", curShare.getGlobalQuote().getHighPrice()),
                String.format("%s", curShare.getGlobalQuote().getLowPrice()),
                String.format("%s", curShare.getGlobalQuote().getPriceChange()),
                curShare.getGlobalQuote().getChangePercent().replace("%", ""),
                String.format("%s", curShare.getGlobalQuote().getVolume()),
                String.format("%.2f", curShare.getPeRatio()),
                String.format("%.2f", curShare.getPbRatio()),
                String.format("%.2f", curShare.getPriceToSales()),
                curShare.getMarketCap(),
                String.format("%.2f", curShare.getEps()),
                String.format("%.2f", curShare.getBookValue())
        ));

        sendMessage(chatId, ansText);
    }

    public void getAsyncSmartAnalyse(long chatId, String ticker) {

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        int messageId = sendMessage(chatId, "\uD83D\uDD04 Запрос обрабатывается");

        AtomicInteger dotCount = new AtomicInteger(1);

        executor.scheduleAtFixedRate(() -> {
            int dots = dotCount.getAndIncrement() % 4; // 0 to 3
            String dotStr = ".".repeat(dots);

            String updatedText = escapeMarkdownSymbols("\uD83D\uDD04 Запрос обрабатывается" + dotStr);
            editMessage(chatId, messageId, updatedText);
        }, 1, 1, TimeUnit.SECONDS);

        shareService.getSmartAnalyse(ticker).thenAccept(result -> {
            executor.shutdownNow();

            deleteMessage(chatId, messageId);

            sendMessage(chatId, escapeMarkdownSymbols(result.substring(1, result.length() - 1)));
        }).exceptionally(ex -> {
            executor.shutdownNow();

            deleteMessage(chatId, messageId);

            sendMessage(chatId, escapeMarkdownSymbols("❌ Ошибка анализа: " + ex.getMessage()));
            return null;
        });
    }

    public String escapeMarkdownSymbols(String text) {
        text = text.replace("\\n", "\n");
        String[] symbols = {"_", "[", "]", "(", ")", "~", ">", "#", "+", "-", "=", "|", ".", "!", ":"};
        for (String symbol : symbols) {
            text = text.replace(symbol, "\\" + symbol);
        }
        return text.replace("**", "*");
    }

    private int sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        try {
            return execute(message).getMessageId();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void editMessage(long chatId, int messageId, String text) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .text(text)
                .parseMode("MarkdownV2")
                .build();

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "⚠️ Не удалось обновить сообщение: " + e.getMessage());
        }
    }

    public void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(Long.toString(chatId))
                .messageId(messageId)
                .build();

        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.warn("⚠️ Не удалось удалить сообщение: {}", e.getMessage(), e);
        }
    }

    public void getChart(long chatId, String ticker) throws TelegramApiException, IOException {
        chartService.generateChart(ticker);
        Path chartPath = Paths.get("tmpDir", ticker + ".png");

        if (!Files.exists(chartPath)) {
            throw new TelegramApiException("Chart file not found at: " + chartPath);
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(chartPath.toFile()));
        execute(photo);

        Files.deleteIfExists(chartPath);
    }
}
