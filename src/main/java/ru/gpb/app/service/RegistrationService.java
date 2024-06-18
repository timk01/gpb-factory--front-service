package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class RegistrationService {

    private final RestTemplate restTemplate;

    @Autowired
    public RegistrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Despite the fact controller of  B service returns general error, i decided to put here one more specific
     * exception handler - see handleHttpStatusCodeException
     * @param request of CreateUserRequest type
     * @return readable by user String (i include specifics only in logs and omit them in returned value)
     */
    public String register(CreateUserRequest request) {
        try {
            log.info("Registry used by userID: {} and userName: {}", request.userId(), request.userName());
            ResponseEntity<Void> response = restTemplate.postForEntity("/users", request, Void.class);
            return handleResponse(response);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e);
        } catch (Exception e) {
            return handleGeneralException(e);
        }
    }

    public String handleResponse(ResponseEntity<Void> response) {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.NO_CONTENT) {
            log.info("User is created");
            return "Пользователь создан";
        } else if (statusCode == HttpStatus.CONFLICT) {
            log.warn("User is already exists: " + response.getBody());
            return "Пользователь уже зарегистрирован: " + statusCode;
        } else {
            log.error("Cannot create user, status: " + response.getBody());
            return "Ошибка при регистрации пользователя: " + statusCode;
        }
    }

    public String handleHttpStatusCodeException(HttpStatusCodeException e) {
        String responseErrorString = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        log.error("Cannot register, HttpStatusCodeException: " + responseErrorString);
        return "Не могу зарегистрировать, ошибка: " + responseErrorString;
    }

    public String handleGeneralException(Exception e) {
        String generalErrorMessage = e.getMessage();
        log.error("Serious exception is happened: " + generalErrorMessage, e);
        return "Произошла серьезная ошибка: " + generalErrorMessage;
    }
}
