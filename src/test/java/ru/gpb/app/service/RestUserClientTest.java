package ru.gpb.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestUserClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestUserClient userClient;

    private CreateUserRequest properRequestId;
    private CreateUserRequest improperRequestId;
    private CreateUserRequest wrongRequestId;

    private String url;

    @BeforeEach
    public void setUp() {
        properRequestId = new CreateUserRequest(868047670, "Khasmamedov");
        improperRequestId = new CreateUserRequest(1234567890, "Khasmamedov");
        wrongRequestId = new CreateUserRequest(-1234567890, "Khasmamedov");
        url = "/users";
    }

    @Test
    public void registerUserWasSuccessful() {
        when(restTemplate.postForEntity(url, properRequestId, Void.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Void> result = userClient.register(properRequestId);

        assertThat(HttpStatus.NO_CONTENT).isEqualTo(result.getStatusCode());
    }

    @Test
    public void registerUserCouldNotBeDone() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(url, improperRequestId, Void.class))
                .thenReturn(response);

        ResponseEntity<Void> result = userClient.register(improperRequestId);

        assertThat(response.getStatusCode()).isEqualTo(result.getStatusCode());
    }

    @Test
    public void openUserInvokedInternalServerException() {
        when(restTemplate.postForEntity(url, wrongRequestId, Void.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> userClient.register(wrongRequestId));
    }

    @Test
    public void openUserInvokedInvokedGeneralException() {
        when(restTemplate.postForEntity(url, wrongRequestId, Void.class))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> userClient.register(wrongRequestId));
    }
}