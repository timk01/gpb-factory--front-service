package ru.gpb.app.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface OutcomingHandler {

    public SendMessage outputtingMessageSender(Message message, String command, String... commandParams);
}
