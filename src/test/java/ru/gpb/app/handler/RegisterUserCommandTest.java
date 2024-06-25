package ru.gpb.app.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserCommandTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterUserCommand command;

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
        assertThat("/register").isEqualTo(result);
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

        CreateUserRequest request = new CreateUserRequest(mockedMessage.getChatId(), "Khasmamedov");
        when(userService.register(request)).thenReturn("Пользователь создан");

        String result = command.executeCommand(mockedMessage);

        assertThat("Пользователь создан").isEqualTo(result);
    }
}