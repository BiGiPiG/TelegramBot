package io.github.bigpig.services;

import io.github.bigpig.dto.ShareDTO;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

@Service
public class MessageService {

    public String generateStartCommand(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        String startText = bundle.getString("startText");

        return escapeMarkdownSymbols(startText);
    }

    public String generateHelpCommand(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        String helpText = bundle.getString("helpText");

        return escapeMarkdownSymbols(helpText);
    }

    public String generateGetValuationMetricsCommand(ShareDTO curShare, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        String template = bundle.getString("valuation.metrics.text");

        String formatted = MessageFormat.format(template,
                curShare.name(),
                curShare.globalQuote().currentPrice(),
                curShare.description().replace("'", "").replace("`", ""),
                curShare.globalQuote().highPrice(),
                curShare.globalQuote().lowPrice(),
                curShare.globalQuote().priceChange(),
                curShare.globalQuote().changePercent().replace("%", ""),
                curShare.globalQuote().volume(),
                String.format("%.2f", curShare.peRatio()),
                String.format("%.2f", curShare.pbRatio()),
                String.format("%.2f", curShare.priceToSales()),
                curShare.marketCap(),
                String.format("%.2f", curShare.eps()),
                String.format("%.2f", curShare.bookValue())
        );

        return escapeMarkdownSymbols(formatted);
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
