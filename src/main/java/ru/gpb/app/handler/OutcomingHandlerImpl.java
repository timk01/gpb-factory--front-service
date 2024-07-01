package ru.gpb.app.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.config.Commandeer;

import java.util.Map;

@Component
@Slf4j
public class OutcomingHandlerImpl implements OutcomingHandler {

    private final Map<String, Command> messageMap;
    private final CommandParamsChecker paramsChecker;

    public OutcomingHandlerImpl(Commandeer commandRegistry, CommandParamsChecker paramsChecker) {
        this.messageMap = commandRegistry.commandMsg();
        this.paramsChecker = paramsChecker;
    }

    @Override
    public SendMessage outputtingMessageSender(Message message, String command, String commandParams) {
                String response;
                if (messageMap.containsKey(command)) {
                    response = paramsChecker.commandParamsCheck(messageMap.get(command), message, commandParams);
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
