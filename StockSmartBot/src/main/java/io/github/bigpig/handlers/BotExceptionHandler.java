package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotExceptionHandler {

    public String handleException(Exception e) {
        switch (e) {
            case ServiceException ex -> {
                return "❌ Произошла ошибка. Попробуйте позже";
            }
            case UserInputException ex -> {
                return "⚠️ Передан некорректный аргумент команды или неизвестная команда.";
            }
            case null, default -> {
                return "❗ Произошла ошибка. Попробуйте снова позже.";
            }
        }
    }
}