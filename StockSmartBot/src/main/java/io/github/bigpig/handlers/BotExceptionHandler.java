package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.CalculateValuationException;
import io.github.bigpig.exceptions.ExternalApiException;
import io.github.bigpig.exceptions.ShareNotFoundException;
import io.github.bigpig.exceptions.SmartAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class BotExceptionHandler {

    public String handleException(Exception e) {
        switch (e) {
            case ShareNotFoundException shareNotFoundException -> {
                return "❌ Не удалось найти информацию по тикеру. Попробуйте другой.";
            }
            case ExternalApiException externalApiException -> {
                return "⚠️ Временная ошибка при получении данных. Повторите попытку позже.";
            }
            case TelegramApiException telegramApiException -> {
                return "⚠️ Не удалось обновить сообщение: " + telegramApiException.getMessage();
            }
            case CalculateValuationException calculateValuationException -> {
                return "⚠️ Не удалось получить информацию по тикеру";
            }
            case SmartAnalysisException smartAnalysisException -> {
                return "⚠️ Ошибка при обработке запроса. Повторите попытку позже";
            }
            case null, default -> {
                return "❗ Произошла ошибка. Попробуйте снова позже.";
            }
        }
    }
}