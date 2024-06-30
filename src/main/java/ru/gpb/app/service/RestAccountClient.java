package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;

@Service
@Slf4j
public class RestAccountClient implements AccountClient {

    private final RestTemplate restTemplate;

    @Autowired
    public RestAccountClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
}
