package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.CalculateValuationException;
import io.github.bigpig.exceptions.ExternalApiException;
import io.github.bigpig.exceptions.ShareNotFoundException;
import io.github.bigpig.exceptions.SmartAnalysisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotExceptionHandler {

    public String handleException(Exception e) {
        switch (e) {
            case ShareNotFoundException shareNotFoundException -> {
                log.warn("Тикер не найден: {}", e.getMessage());
                return "❌ Не удалось найти информацию по тикеру. Попробуйте другой.";
            }
            case ExternalApiException externalApiException -> {
                log.error("Ошибка обращения к внешнему API: {}", externalApiException.getMessage(), externalApiException);
                return "⚠️ Временная ошибка при получении данных. Пожалуйста, повторите попытку позже.";
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
                log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
                return "❗ Произошла ошибка. Попробуйте снова позже.";
            }
        }
    }
}