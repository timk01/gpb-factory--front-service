package ru.gpb.app.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class HelpCommand implements Command {
    @Override
    public String getBotCommand() {
        return "/help";
    }

    @Override
    public boolean needsServiceInteraction() {
        return false;
    }

    @Override
    public String executeCommand(Message message) {
        return "no help for you now, use '/ping' command instead";

    }
}
