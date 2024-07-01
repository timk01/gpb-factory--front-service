package ru.gpb.app.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@Slf4j
public class CommandParamsChecker {

    /**
     * Beware: in case you forgot to put annotation on class, it will throw exception immediately!
     * @param command of type Command (any child of command)
     * @param message of type Message (any whole message)
     * @param commandParams (in case message consists solo of command, where won't be any params)
     * @return executed command-answer of type String
     */
    public String commandParamsCheck(Command command, Message message, String... commandParams) {
        ExpectedCommandParams annotation = command.getClass().getAnnotation(ExpectedCommandParams.class);

        if (annotation == null) {
            String currClass = command.getClass().getName();
            log.error("Whoever get this log error, forgot annotation on {}", currClass);
            return "Ошибка: забыли аннотацию ExpectedCommandParams над классом: " + currClass;
        }

        int providedParams = (commandParams.length == 1 && commandParams[0].isEmpty()) ? 0 : commandParams.length;
        int supposedParams = annotation.value();
        if (providedParams != supposedParams) {
            log.warn("Wrong params quantity: supposed to be {}, but was: {}", supposedParams, providedParams);
            return "Команда " + command.getBotCommand() + " должна содержать " + supposedParams + " параметра(ов)";
        }

        return command.executeCommand(message, commandParams);
    }
}
