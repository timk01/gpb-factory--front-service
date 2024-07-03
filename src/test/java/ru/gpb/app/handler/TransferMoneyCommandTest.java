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
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.service.AccountService;
import ru.gpb.app.dto.Error;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMoneyCommandTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransferMoneyCommand command;

    private String[] transferData;

    private CreateTransferRequestDto request;

    private User user;

    private Message mockedMessage;

    @BeforeEach
    public void setUp() {
        mockedMessage = mock(Message.class);

        transferData = new String[]{"paveldurov", "203605.20"};

        user = new User();
        user.setId(878647670L);
        user.setUserName("Khasmamedov");

        request = new CreateTransferRequestDto("Khasmamedov", 878647670L, "paveldurov", "203605.20");
    }

    @Test
    public void getBotCommandSucceed() {
        String result = command.getBotCommand();
        assertThat("/transfer").isEqualTo(result);
    }

    @Test
    public void transferCommandWasNotExecutedDueToWrongSum() {
        String result = command.executeCommand(mockedMessage, "paveldurov", "fivaproldg");

        assertThat("Неверный формат суммы - должен быть дробный формат типа 123.456")
                .isEqualTo(result);
    }

    @Test
    public void transferCommandWasExecuted() {
        when(mockedMessage.getFrom()).thenReturn(user);

        when(accountService.makeAccountTransfer(request))
                .thenReturn("Перевод успешно выполнен, ID перевода: " + "52d2ef91-0b62-4d43-bb56-e7ec542ba8f8");

        String result = command.executeCommand(mockedMessage, transferData);

        assertThat("Перевод успешно выполнен, ID перевода: " + "52d2ef91-0b62-4d43-bb56-e7ec542ba8f8")
                .isEqualTo(result);
    }

    @Test
    public void transferCommandWasNotExecuted() {
        when(mockedMessage.getFrom()).thenReturn(user);

        when(accountService.makeAccountTransfer(request)).thenReturn("Не могу совершить денежный перевод: " + "409");

        String result = command.executeCommand(mockedMessage, transferData);

        assertThat("Не могу совершить денежный перевод: " + "409").isEqualTo(result);
    }

    @Test
    public void transferCommandWasNotExecutedDueToHttpStatusCodeException() {
        when(mockedMessage.getFrom()).thenReturn(user);

        Error userCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );

        String expectedResponse = "Не могу выполнить денежный перевод, ошибка: " + convertErrorToJson(userCreationError);
        when(accountService.makeAccountTransfer(request)).thenReturn(expectedResponse);

        String result = command.executeCommand(mockedMessage, transferData);

        assertThat(expectedResponse).isEqualTo(result);
    }

    @Test
    public void transferCommandWasNotExecutedDueToGeneralException() {
        when(mockedMessage.getFrom()).thenReturn(user);

        String expectedResponse = "Произошла серьезная ошибка во время выполнения денежного перевода: Unexpected error";

        when(accountService.makeAccountTransfer(request)).thenReturn(expectedResponse);

        String result = command.executeCommand(mockedMessage, transferData);

        assertThat(expectedResponse).isEqualTo(result);
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
