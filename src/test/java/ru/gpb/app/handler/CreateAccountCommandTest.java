package ru.gpb.app.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.service.AccountService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAccountCommandTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CreateAccountCommand command;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(123L);
        user.setUserName("Khasmamedov");
    }

    @Test
    public void getBotCommandSucceed() {
        String result = command.getBotCommand();
        assertThat("/createaccount").isEqualTo(result);
    }

    @Test
    public void serviceInteractionServiceReturnedTrue() {
        boolean result = command.needsServiceInteraction();
        assertThat(result).isTrue();
    }

    @Test
    public void executeCommandRunFine() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);
        when(mockedMessage.getFrom()).thenReturn(user);

        CreateAccountRequest request = new CreateAccountRequest(
                mockedMessage.getChatId(),
                user.getUserName(),
                "My first awesome account"
        );
        when(accountService.openAccount(request)).thenReturn("Счет создан");

        String result = command.executeCommand(mockedMessage);

        assertThat("Счет создан").isEqualTo(result);
    }
}