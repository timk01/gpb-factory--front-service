package ru.gpb.app.service;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import ru.gpb.app.dto.*;
import ru.gpb.app.dto.Error;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountClient accountClient;
    @InjectMocks
    private AccountService service;
    private CreateAccountRequest accountRequest;

    private Long userId;

    private CreateTransferRequest transferRequest;

    @BeforeEach
    public void setUp() {
        userId = 868047670L;
        accountRequest = new CreateAccountRequest(
                userId,
                "Khasmamedov",
                "My first awesome account"
        );
        transferRequest = new CreateTransferRequest("Khasmamedov", "Durov", "100");
    }

    @Test
    public void registerAccountWasSuccessful() {
        when(accountClient.openAccount(accountRequest)).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        String result = service.openAccount(accountRequest);

        assertThat("Счет создан").isEqualTo(result);
    }

    @Test
    public void registerAccountWasAlreadyDoneBefore() {
        when(accountClient.openAccount(accountRequest))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        String result = service.openAccount(accountRequest);

        assertThat("Счет уже зарегистрирован: " + HttpStatus.CONFLICT).isEqualTo(result);
    }

    @Test
    public void gettingAccountsWasOK() {
        AccountListResponse[] accounts = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };
        ResponseEntity<AccountListResponse[]> responseEntity = new ResponseEntity<>(accounts, HttpStatus.OK);
        when(accountClient.getAccount(userId))
                .thenReturn(responseEntity);

        String result = service.getAccount(userId);

        String expected = "Список счетов пользователя: " + Arrays.asList(accounts);

        assertThat(expected).isEqualTo(result);
    }

    @Test
    public void gettingAccountsReturnedNoData() {
        AccountListResponse[] accounts = {};
        ResponseEntity<AccountListResponse[]> responseEntity = new ResponseEntity<>(accounts, HttpStatus.NO_CONTENT);
        when(accountClient.getAccount(userId)).thenReturn(responseEntity);

        String result = service.getAccount(userId);

        assertThat("Нет счетов у пользователя").isEqualTo(result);
    }

    @Test
    public void registerAccountProcessCouldNotBeDone() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(accountClient.openAccount(accountRequest)).thenReturn(response);

        String result = service.openAccount(accountRequest);

        assertThat("Ошибка при создании счета: " + response.getStatusCode()).isEqualTo(result);
    }

    @Test
    public void gettingAccountProcessCouldNotBeDone() {
        ResponseEntity<AccountListResponse[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(accountClient.getAccount(userId))
                .thenReturn(responseEntity);

        String result = service.getAccount(userId);

        assertThat("Не могу получить счета (пустой ответ // не найдено счетов)").isEqualTo(result);
    }

    @Test
    public void registerAccountInvokedInternalServerException() {
        Error accountCreationError = new Error(
                "Ошибка создания счета",
                "AccountCreationError",
                "500",
                UUID.randomUUID()
        );
        String jsonError = convertErrorToJson(accountCreationError);
        HttpStatusCodeException httpStatusCodeException =
                new HttpServerErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        null,
                        jsonError.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                );
        when(accountClient.openAccount(accountRequest)).thenThrow(httpStatusCodeException);

        String result = service.openAccount(accountRequest);

        assertThat("Не могу зарегистрировать счет, ошибка: " + jsonError).isEqualTo(result);
    }

    @Test
    void getAccountHandledHttpStatusCodeException() {
        Error accountCreationError = new Error(
                "Ошибка получения счета",
                "AccountGettingError",
                "500",
                UUID.randomUUID()
        );
        String jsonError = convertErrorToJson(accountCreationError);
        HttpStatusCodeException httpStatusCodeException =
                new HttpServerErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        null,
                        jsonError.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                );

        when(accountClient.getAccount(userId))
                .thenThrow(httpStatusCodeException);

        String result = service.getAccount(userId);

        assertThat("Не могу получить счета, ошибка: " + jsonError).isEqualTo(result);
    }

    @Test
    public void registerAccountInvokedGeneralException() {
        Error accountCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );
        String jsonError = convertErrorToJson(accountCreationError);
        RuntimeException generalException =
                new RuntimeException(jsonError);

        when(accountClient.openAccount(accountRequest)).thenThrow(generalException);;

        String result = service.openAccount(accountRequest);

        assertThat("Произошла серьезная ошибка во время создания счета: " + jsonError).isEqualTo(result);
    }

    @Test
    void getAccountHandledGeneralException() {
        Error accountCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );
        String jsonError = convertErrorToJson(accountCreationError);
        RuntimeException generalException =
                new RuntimeException(jsonError);

        when(accountClient.getAccount(userId)).thenThrow(generalException);;

        String result = service.getAccount(userId);

        assertThat("Произошла серьезная ошибка во время получения счетов: " + jsonError).isEqualTo(result);
    }

    @Test
    public void makeTransferWasSuccessful() {
        CreateTransferResponse transferResponse = new CreateTransferResponse("12345");
        when(accountClient.makeAccountTransfer(transferRequest))
                .thenReturn(new ResponseEntity<>(transferResponse, HttpStatus.OK));

        String result = service.makeAccountTransfer(transferRequest);

        assertThat("Перевод успешно выполнен, ID: " + transferResponse.transferId()).isEqualTo(result);
    }

    @Test
    public void makeTransferCouldNoBeDone() {
        CreateTransferResponse transferResponse = new CreateTransferResponse("12345");
        when(accountClient.makeAccountTransfer(transferRequest))
                .thenReturn(new ResponseEntity<>(transferResponse, HttpStatus.CONFLICT));

        String result = service.makeAccountTransfer(transferRequest);

        assertThat("Не могу совершить денежный перевод: " + HttpStatus.CONFLICT).isEqualTo(result);
    }

    @Test
    public void makeTransferGotHttpStatusCodeException() {
        HttpStatusCodeException exception = new HttpStatusCodeException(HttpStatus.CONFLICT) {};
        String responseErrorString = new String(exception.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        when(accountClient.makeAccountTransfer(transferRequest)).thenThrow(exception);

        String result = service.makeAccountTransfer(transferRequest);

        assertThat("Не могу выполнить денежный перевод, ошибка: " + responseErrorString).isEqualTo(result);
    }

    @Test
    public void makeTransferGotGeneralException() {
        Exception exception = new RuntimeException("Error");
        when(accountClient.makeAccountTransfer(transferRequest)).thenThrow(exception);

        String result = service.makeAccountTransfer(transferRequest);

        assertThat("Произошла серьезная ошибка во время выполнения денежного перевода: " + exception.getMessage()
        ).isEqualTo(result);
    }

    public static String convertErrorToJson(Error error) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}

