package ru.gpb.app.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.UserService;

@Component
public class RegisterUserCommand implements Command {

    private final UserService userService;

    public RegisterUserCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getBotCommand() {
        return "/register";
    }

    @Override
    public String executeCommand(Message message) {
        CreateUserRequest request = new CreateUserRequest(message.getChatId(), message.getFrom().getUserName());
        return userService.register(request);
    }
}

