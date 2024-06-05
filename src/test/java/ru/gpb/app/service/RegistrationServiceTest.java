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
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.dto.Error;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegistrationService service;

    private CreateUserRequest properRequestId;
    private CreateUserRequest improperRequestId;
    private CreateUserRequest wrongRequestId;

    @BeforeEach
    public void setUp() {
        properRequestId = new CreateUserRequest(868047670);
        improperRequestId = new CreateUserRequest(1234567890);
        wrongRequestId = new CreateUserRequest(-1234567890);
    }

    @Test
    public void registerResponseIsOKAndReturnedCodeIsGood() {
        when(restTemplate.postForEntity("/users", properRequestId, Void.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        String result = service.register(properRequestId);

        assertThat("Пользователь создан").isEqualTo(result);
        verify(restTemplate, times(1))
                .postForEntity("/users", properRequestId, Void.class);
    }

    @Test
    public void registerResponseIsOKAndReturnedCodeIsBad() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity("/users", improperRequestId, Void.class))
                .thenReturn(response);

        String result = service.register(improperRequestId);

        assertThat("Непредвиденная ошибка: " + response.getStatusCode()).isEqualTo(result);
        verify(restTemplate, times(1))
                .postForEntity("/users", improperRequestId, Void.class);
    }

    @Test
    public void registerResponseInvokedInternalServerException() {
        Error userCreationError = new Error(
                "Ошибка регистрации пользователя",
                "UserCreationError",
                "500",
                UUID.randomUUID()
        );

        String jsonError = convertErrorToJson(userCreationError);
        HttpStatusCodeException httpStatusCodeException =
                new HttpServerErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        null,
                        jsonError.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.UTF_8
                );

        when(restTemplate.postForEntity("/users", wrongRequestId, Void.class))
                .thenThrow(httpStatusCodeException);

        String result = service.register(wrongRequestId);

        assertThat("Не могу зарегистрировать, ошибка: " + jsonError).isEqualTo(result);
        verify(restTemplate, times(1))
                .postForEntity("/users", wrongRequestId, Void.class);
    }

    @Test
    public void registerResponseInvokedGeneralException() {
        Error userCreationError = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );

        String jsonError = convertErrorToJson(userCreationError);

        RuntimeException generalException =
                new RuntimeException(jsonError);

        when(restTemplate.postForEntity("/users", wrongRequestId, Void.class))
                .thenThrow(generalException);

        String result = service.register(wrongRequestId);

        assertThat("Произошла серьезная ошибка: " + jsonError).isEqualTo(result);
        verify(restTemplate, times(1))
                .postForEntity("/users", wrongRequestId, Void.class);
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