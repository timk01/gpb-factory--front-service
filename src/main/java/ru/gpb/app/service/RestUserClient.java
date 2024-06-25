package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;

@Service
@Slf4j
public class RestUserClient implements UserClient {

    private final RestTemplate restTemplate;

    public RestUserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<Void> register(CreateUserRequest request) {
        log.info("Using restTemplate for creating user");
        return restTemplate.postForEntity("/users", request, Void.class);
    }
}
