package io.github.bigpig.services;

import io.github.bigpig.dto.ShareDTO;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    public String generateStartCommand() {
        String startText = """
        🚀 *Добро пожаловать в MarketInsightBot!* 🚀

        *Ваш персональный помощник для анализа рынков:*
        - 📊 Ключевые метрики (P/E, P/S и др.)
        - 🤖 AI-анализ
        - 📈 Визуализация данных

        🔍 *Как начать?*
        Просто введите команду с тикером интересующего актива:

        `/getValuationMetrics IBM` - анализ IBM
        `/getSmartAnalyse IBM` - AI-разбор IBM

        📌 Для всех команд напишите /help
        """;

        return escapeMarkdownSymbols(startText);
    }

    public String generateHelpCommand() {
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
        `/getValuationMetrics IBM` - анализ IBM
        `/getSmartAnalyse IBM` - AI-разбор IBM
        `/getChart IBM` - график для IBM
        """;

        return escapeMarkdownSymbols(helpText);
    }

    public String generateGetValuationMetricsCommand(ShareDTO curShare) {
        String valuationMetricsText = String.format("""
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
                String.format("%s", curShare.getGlobalQuote().currentPrice()),
                String.format("%s", curShare.getDescription().replace("'", "").replace("`", "")),
                String.format("%s", curShare.getGlobalQuote().highPrice()),
                String.format("%s", curShare.getGlobalQuote().lowPrice()),
                String.format("%s", curShare.getGlobalQuote().priceChange()),
                curShare.getGlobalQuote().changePercent().replace("%", ""),
                String.format("%s", curShare.getGlobalQuote().volume()),
                String.format("%.2f", curShare.getPeRatio()),
                String.format("%.2f", curShare.getPbRatio()),
                String.format("%.2f", curShare.getPriceToSales()),
                curShare.getMarketCap(),
                String.format("%.2f", curShare.getEps()),
                String.format("%.2f", curShare.getBookValue())
        );

        return escapeMarkdownSymbols(valuationMetricsText);
    }

    public String generateProcessingMessage(int dotCount) {
        String dots = ".".repeat(dotCount % 4); // от 0 до 3
        return escapeMarkdownSymbols("⏳ Запрос обрабатывается" + dots);
    }

    public String generateSmartAnalyseResult(String result) {
        return escapeMarkdownSymbols(result.substring(1, result.length() - 1));
    }

    public String generateErrorMessage(String msg) {
        return escapeMarkdownSymbols(msg);
    }

    public String escapeMarkdownSymbols(String text) {
        text = text.replace("\\n", "\n");
        String[] symbols = {"_", "[", "]", "(", ")", "~", ">", "#", "+", "-", "=", "|", ".", "!", ":"};
        for (String symbol : symbols) {
            text = text.replace(symbol, "\\" + symbol);
        }
        return text.replace("**", "*");
    }
}
