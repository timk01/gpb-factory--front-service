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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.Error;

import java.nio.charset.StandardCharsets;
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

    @BeforeEach
    public void setUp() {
        accountRequest = new CreateAccountRequest(
                123L,
                "Khasmamedov",
                "My first awesome account"
        );
    }

    @Test
    public void registerAccountWasSuccessful() {
        when(accountClient.openAccount(accountRequest)).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        String result = service.openAccount(accountRequest);

        assertThat("Счет создан").isEqualTo(result);
    }

    @Test
    public void registerAccountWasAlreadyDoneBefore() {
        when(accountClient.openAccount(accountRequest)).thenReturn(new ResponseEntity<>(HttpStatus.CONFLICT));

        String result = service.openAccount(accountRequest);

        assertThat("Такой счет у данного пользователя уже есть: " + HttpStatus.CONFLICT).isEqualTo(result);
    }

    @Test
    public void registerAccountProcessCouldNotBeDone() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(accountClient.openAccount(accountRequest)).thenReturn(response);;

        String result = service.openAccount(accountRequest);

        assertThat("Ошибка при создании счета: " + response.getStatusCode()).isEqualTo(result);
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
        when(accountClient.openAccount(accountRequest)).thenThrow(httpStatusCodeException);;

        String result = service.openAccount(accountRequest);

        assertThat("Не могу зарегистрировать счет, ошибка: " + jsonError).isEqualTo(result);
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
