package io.github.bigpig.handlers;

import io.github.bigpig.exceptions.*;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Setter
@Component
public class BotExceptionHandler {

    private Locale locale;

    public BotExceptionHandler() {
        this.locale = Locale.ENGLISH;
    }

    private ResourceBundle getMessages() {
        return ResourceBundle.getBundle("messages", locale);
    }

    public String handleException(Exception e) {
        ResourceBundle messages = getMessages();
        String key = switch (e) {
            case ServiceException ex -> "error.service";
            case UserInputException ex -> "error.user_input";
            case null, default -> "error.unknown";
        };
        return messages.getString(key);
    }
}