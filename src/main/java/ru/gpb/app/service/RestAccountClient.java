package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;

@Service
@Slf4j
public class RestAccountClient implements AccountClient {

    private final RestTemplate restTemplate;
    private final WebClientService webClientService;

    public RestAccountClient(RestTemplate restTemplate, WebClient webClient, WebClientService webClientService) {
        this.restTemplate = restTemplate;
        this.webClientService = webClientService;
    }

    public ResponseEntity<Void> openAccount(CreateAccountRequest request) {

        log.info("Using restTemplate for creating account");
        String url = String.format("/users/%d/accounts", request.userId());
        return restTemplate.postForEntity(url, request, Void.class);
    }

    @Override
    public  ResponseEntity<AccountListResponse[]> getAccount(Long chatId) {
        log.info("Using restTemplate for getting account");
        String url = String.format("/users/%d/accounts", chatId);
        return restTemplate.getForEntity(url, AccountListResponse[].class);
    }


    @Override
    public ResponseEntity<CreateTransferResponse> makeAccountTransfer(CreateTransferRequestDto request) {
        log.info("Using WebClient for transferring money");
        return webClientService.makeAccountTransfer(request);
    }
}
