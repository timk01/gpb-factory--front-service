package ru.gpb.app.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class PingCommand implements Command {
    @Override
    public String getBotCommand() {
        return "/ping";
    }

    @Override
    public boolean needsServiceInteraction() {
        return false;
    }

    @Override
    public String executeCommand(Message message) {
        return "pong";
    }
}

