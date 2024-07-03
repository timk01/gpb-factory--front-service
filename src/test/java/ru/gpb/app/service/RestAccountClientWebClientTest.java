package ru.gpb.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestAccountClientWebClientTest {

    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private RestAccountClient accountClient;

    private CreateTransferRequestDto transferRequest;
    private CreateTransferResponse transferResponse;

    @BeforeEach
    public void setUp() {
        transferRequest = new CreateTransferRequestDto("Khasmamedov", 878647670L, "Durov", "100");
        transferResponse = new CreateTransferResponse("12345");
    }

    @Test
    public void makeTransferWasSuccessful() {
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenReturn(new ResponseEntity<>(transferResponse, HttpStatus.OK));

        ResponseEntity<CreateTransferResponse> result = accountClient.makeAccountTransfer(transferRequest);

        assertThat(result.getBody().transferId()).isEqualTo("12345");
    }

    @Test
    public void makeTransferFailed() {
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenReturn(new ResponseEntity<>(transferResponse, HttpStatus.BAD_REQUEST));

        ResponseEntity<CreateTransferResponse> result = accountClient.makeAccountTransfer(transferRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void makeTransferGotHttpStatusCodeException() {
        HttpStatusCodeException exception = new HttpStatusCodeException(HttpStatus.CONFLICT) {};
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenThrow(exception);

        assertThrows(HttpStatusCodeException.class, () -> {
            accountClient.makeAccountTransfer(transferRequest);
        });
    }

    @Test
    public void makeTransferGotGeneralException() {
        RuntimeException exception = new RuntimeException("Error");
        when(webClientService.makeAccountTransfer(transferRequest)).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> {
            accountClient.makeAccountTransfer(transferRequest);
        });
    }
}