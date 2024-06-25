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
import ru.gpb.app.dto.CreateAccountRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestAccountClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestAccountClient accountClient;

    private CreateAccountRequest accountRequest;

    private String url;

    @BeforeEach
    public void setUp() {
        accountRequest = new CreateAccountRequest(
                123L,
                "Khasmamedov",
                "My first awesome account"
        );
        url = String.format("/users/%d/accounts", accountRequest.userId());
    }

    @Test
    public void openAccountWasSuccessful() {
        when(restTemplate.postForEntity(url, accountRequest, Void.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Void> response = accountClient.openAccount(accountRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void openAccountWasAlreadyDoneBefore() {
        when(restTemplate.postForEntity(url, accountRequest, Void.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        ResponseEntity<Void> result = accountClient.openAccount(accountRequest);

        assertThat(HttpStatus.NO_CONTENT).isEqualTo(result.getStatusCode());
    }

    @Test
    public void openAccountCouldNotBeDone() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(url, accountRequest, Void.class))
                .thenReturn(response);

        ResponseEntity<Void> result = accountClient.openAccount(accountRequest);

        assertThat(response.getStatusCode()).isEqualTo(result.getStatusCode());
    }

    @Test
    public void openAccountInvokedInternalServerException() {
        when(restTemplate.postForEntity(url, accountRequest, Void.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> accountClient.openAccount(accountRequest));
    }

    @Test
    public void openAccountInvokedInvokedGeneralException() {
        when(restTemplate.postForEntity(url, accountRequest, Void.class))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> accountClient.openAccount(accountRequest));
    }
}