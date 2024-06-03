package ru.gpb.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.config.Commandeer;

import java.util.Map;

@Component
public class OutcomingHandlerImpl implements OutcomingHandler {

    private final Map<String, Command> messageMap;

    @Autowired
    public OutcomingHandlerImpl(Commandeer commandRegistry) {
        this.messageMap = commandRegistry.commandMsg();
    }

    @Override
    public SendMessage outputtingMessageSender(Message message) {
        String response = messageMap.containsKey(message.getText()) ?
                messageMap.get(message.getText()).executeTextCommand() : "where is no place like home";
        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text(response)
                .build();
    }
}
