package ru.gpb.app.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.config.Commandeer;

import java.util.Map;

@Component
@Slf4j
public class OutcomingHandlerImpl implements OutcomingHandler {

    private final Map<String, Command> messageMap;

    @Autowired
    public OutcomingHandlerImpl(Commandeer commandRegistry) {
        this.messageMap = commandRegistry.commandMsg();
    }

    @Override
    public SendMessage outputtingMessageSender(Message message) {
        String command = message.getText();
        String response;
        if (messageMap.containsKey(command)) {
            response = messageMap.get(command).executeCommand(message);
        } else {
            response = "no such command";
        }
        log.info("Processed command: '{}', response: '{}'", command, response);
        return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text(response)
                .build();
    }
}
