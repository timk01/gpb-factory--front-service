package ru.gpb.app.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.Error;
import ru.gpb.app.service.AccountService;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountCommandTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private GetAccountCommand command;

    private static Long userId;
    
    @BeforeEach
    public void setUp() {
        userId = 123L;
    }

    @Test
    public void getBotCommandSucceed() {
        String result = command.getBotCommand();
        assertThat("/currentbalance").isEqualTo(result);
    }


    @Test
    public void executeCommandReturnedAccount() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(userId);

        AccountListResponse[] responses = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };
        String userAccounts = "Список счетов пользователя: " + Arrays.asList(responses);
        when(accountService.getAccount(userId))
                .thenReturn(userAccounts);

        String result = command.executeCommand(mockedMessage);

        assertThat(userAccounts).isEqualTo(result);
    }

    @Test
    public void executeCommandReturnedNoAccounts() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(userId);

        when(accountService.getAccount(userId)).thenReturn("Нет счетов у пользователя");

        String result = command.executeCommand(mockedMessage);

        assertThat("Нет счетов у пользователя").isEqualTo(result);
    }

    @Test
    public void executeCommandReturnedNoDefiniteAnswer() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(userId);

        when(accountService.getAccount(userId))
                .thenReturn("Не могу получить счета (пустой ответ // не найдено счетов)");

        String result = command.executeCommand(mockedMessage);

        assertThat(result)
                .isEqualTo("Не могу получить счета (пустой ответ // не найдено счетов)");
    }

    @Test
    public void executeCommandReturnedWithHttpStatusCodeException() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(userId);

        Error userCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );
        String expectedResponse = "Не могу получить счета, ошибка: " + convertErrorToJson(userCreationError);

        when(accountService.getAccount(userId))
                .thenReturn(expectedResponse);

        String result = command.executeCommand(mockedMessage);

        assertThat(expectedResponse)
                .isEqualTo(result);
    }

    @Test
    public void executeCommandGotGeneralException() {
        Message mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);

        String expectedResponse = "Произошла серьезная ошибка во время получения счетов: Unexpected error";
        when(accountService.getAccount(123L)).thenReturn(expectedResponse);

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