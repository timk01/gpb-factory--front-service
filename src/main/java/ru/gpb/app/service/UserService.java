package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import ru.gpb.app.dto.CreateUserRequest;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    /**
     * Despite the fact controller of  B service returns general error, i decided to put here one more specific
     * exception handler - see handleHttpStatusCodeException
     *
     * @param response<Void> of CreateUserRequest type
     * @return readable by user String (i include specifics only in logs and omit them in returned value)
     */

    private String handleResponse(ResponseEntity<Void> response) {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.NO_CONTENT) {
            log.info("User is created");
            return "Пользователь создан";
        }
        log.error("Cannot create user, status: " + response.getBody());
        return "Ошибка при регистрации пользователя: " + statusCode;
    }

    private String handleHttpStatusCodeException(HttpStatusCodeException e) {
        String message;
        String responseErrorString = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            log.error("User already exists: {}", responseErrorString);
            message = "Пользователь уже зарегистрирован: " + HttpStatus.CONFLICT;
        } else {
            log.error("Cannot register, HttpStatusCodeException: " + responseErrorString);
            message = "Не могу зарегистрировать пользователя, ошибка: " + responseErrorString;
        }
        return message;
    }

    private String handleGeneralException(Exception e) {
        String generalErrorMessage = e.getMessage();
        log.error("Serious exception is happened: " + generalErrorMessage, e);
        return "Произошла серьезная ошибка: " + generalErrorMessage;
    }

    public String register(CreateUserRequest request) {
        try {
            log.info("Registry used by userID: {} and userName: {}", request.userId(), request.userName());
            return handleResponse(userClient.register(request));
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e);
        } catch (Exception e) {
            return handleGeneralException(e);
        }
    }
}
