package ru.gpb.app.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command {
    String getBotCommand();

    public String executeCommand(Message message);
}
