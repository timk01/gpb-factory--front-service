package ru.gpb.app.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpb.app.handler.OutcomingHandler;

@Slf4j
@Component
public class AppBankBotComponent extends TelegramLongPollingBot {
    private final String botUsername;
    private final OutcomingHandler outcomingHandler;

    @Autowired
    public AppBankBotComponent(
            @Value("${bot.name}") String botUsername,
            @Value("${bot.token}") String botToken,
            OutcomingHandler outcomingHandler) {
        super(botToken);
        this.botUsername = botUsername;
        this.outcomingHandler = outcomingHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                SendMessage message = outcomingHandler.outputtingMessageSender(update.getMessage());
                execute(message);
                log.info("'{}' message just send to chat '{}'", message.getText(), message.getChatId());
            } catch (TelegramApiException e) {
                log.error("Something wrong with delivering message by bot: ", e);
            }
        }
    }
}

