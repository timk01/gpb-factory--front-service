package ru.gpb.app.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.Error;
import ru.gpb.app.service.AccountService;

import java.util.UUID;

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
    public void getBotCommandSuccess() {
        String result = command.getBotCommand();
        assertThat("/createaccount").isEqualTo(result);
    }

    @Test
    public void executeCommandCreatedAccount() {
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

    @Test
    public void executeCommandDidntCreateAccountDueToAccConflict() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);
        when(mockedMessage.getFrom()).thenReturn(user);

        CreateAccountRequest request = new CreateAccountRequest(
                mockedMessage.getChatId(),
                user.getUserName(),
                "My first awesome account"
        );
        String response = "Такой счет у данного пользователя уже есть: " + "409";
        when(accountService.openAccount(request)).thenReturn(response);

        String result = command.executeCommand(mockedMessage);

        assertThat(response).isEqualTo(result);
    }

    @Test
    public void executeCommandDidntCreateAccountDueToError() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);
        when(mockedMessage.getFrom()).thenReturn(user);

        @SuppressWarnings("unchecked")
        ResponseEntity<Void> mockedResponse = mock(ResponseEntity.class);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        CreateAccountRequest request = new CreateAccountRequest(
                mockedMessage.getChatId(),
                user.getUserName(),
                "My first awesome account"
        );

        String response = "Ошибка при создании счета: " + mockedResponse.getStatusCode();
        when(accountService.openAccount(request)).thenReturn(response);

        String result = command.executeCommand(mockedMessage);

        assertThat(response).isEqualTo(result);
    }

    @Test
    public void executeCommandReturnedWithHttpStatusCodeException() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);
        when(mockedMessage.getFrom()).thenReturn(user);

        CreateAccountRequest request = new CreateAccountRequest(
                mockedMessage.getChatId(),
                user.getUserName(),
                "My first awesome account"
        );

        Error userCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );
        String expectedResponse = "Не могу зарегистрировать счет, ошибка: " + convertErrorToJson(userCreationError);

        when(accountService.openAccount(request)).thenReturn(expectedResponse);

        String result = command.executeCommand(mockedMessage);

        assertThat(expectedResponse).isEqualTo(result);
    }

    @Test
    public void executeCommandGotGeneralException() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);
        when(mockedMessage.getFrom()).thenReturn(user);

        CreateAccountRequest request = new CreateAccountRequest(
                mockedMessage.getChatId(),
                user.getUserName(),
                "My first awesome account"
        );

        String expectedResponse = "Произошла серьезная ошибка во время создания счета: Unexpected error";
        when(accountService.openAccount(request)).thenReturn(expectedResponse);

        String result = command.executeCommand(mockedMessage);

        assertThat(result).isEqualTo(expectedResponse);
    }

    public static String convertErrorToJson(Error error) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}