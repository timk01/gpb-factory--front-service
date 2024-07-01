package ru.gpb.app.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.service.AccountService;

@ExpectedCommandParams(0)
@Component
public class CreateAccountCommand implements Command {

    private final AccountService accountService;

    public CreateAccountCommand(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public String getBotCommand() {
        return "/createaccount";
    }

    @Override
    public String executeCommand(Message message, String... params) {
        CreateAccountRequest accountRequest = new CreateAccountRequest(
                message.getChatId(),
                message.getFrom().getUserName(),
                "My first awesome account"
        );
        return accountService.openAccount(accountRequest);
    }
}
