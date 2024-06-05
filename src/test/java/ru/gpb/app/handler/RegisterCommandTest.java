package ru.gpb.app.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.RegistrationService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCommandTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegisterCommand command;

    @Test
    public void getBotCommandSuccess() {
        String result = command.getBotCommand();
        assertThat("/register").isEqualTo(result);
    }

    @Test
    public void serviceInteractionServiceReturnsTrue() {
        boolean result = command.needsServiceInteraction();
        assertThat(result).isTrue();
    }

    @Test
    public void executeCommandRunsFine() {
        Message mockedMessage = mock(Message.class);

        when(mockedMessage.getChatId()).thenReturn(123L);

        CreateUserRequest request = new CreateUserRequest(mockedMessage.getChatId());
        when(registrationService.register(request)).thenReturn("Success");

        String result = command.executeCommand(mockedMessage);

        assertThat("Success").isEqualTo(result);
    }

}