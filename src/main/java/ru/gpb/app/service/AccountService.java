package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferResponse;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class AccountService {

    private final AccountClient accountClient;

    public AccountService(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    private String handleResponse(ResponseEntity<Void> response) {
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.NO_CONTENT) {
            log.info("Account is created");
            return "Счет создан";
        }
        log.error("Cannot create account, status: " + response.getBody());
        return "Ошибка при создании счета: " + statusCode;
    }

    private String handleHttpStatusCodeException(HttpStatusCodeException e) {
        String message;
        String responseErrorString = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            log.error("Account already exists: {}", responseErrorString);
            message = "Счет уже зарегистрирован: " + HttpStatus.CONFLICT;
        } else {
            log.error("Cannot register account, HttpStatusCodeException: " + responseErrorString);
            return "Не могу зарегистрировать счет, ошибка: " + responseErrorString;
        }
        return message;
    }

    private String handleGeneralException(Exception e) {
        String generalErrorMessage = e.getMessage();
        log.error("Serious exception is happened: " + generalErrorMessage, e);
        return "Произошла серьезная ошибка во время создания счета: " + generalErrorMessage;
    }

    public String openAccount(CreateAccountRequest request) {
        log.info("Creating account for userID: {} with accountName: {}", request.userId(), request.accountName());
        try {
            ResponseEntity<Void> response = accountClient.openAccount(request);
            log.info("sending request to middle to create acc " + response);
            return handleResponse(response);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e);
        } catch (Exception e) {
            return handleGeneralException(e);
        }
    }

    private String handleGetAccountResponse(Optional<ResponseEntity<AccountListResponse[]>> response) {
        if (response.isPresent() && response.get().getBody() != null) {
            AccountListResponse[] responses = response.get().getBody();
            log.info("Users accounts found: {}", Arrays.asList(responses));
            return "Список счетов пользователя: " + Arrays.asList(responses);
        } else {
            log.error("Cannot retrieve account details (empty response or no accounts were found)");
            return "Не могу получить счета (пустой ответ // не найдено счетов)";
        }
    }

    private String handleGetAccountHttpStatusCodeException(HttpStatusCodeException e) {
        String responseErrorString = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        log.error("Cannot get accounts, HttpStatusCodeException: " + responseErrorString);
        return "Не могу получить счета, ошибка: " + responseErrorString;
    }

    private String handleGetAccountGeneralException(Exception e) {
        String generalErrorMessage = e.getMessage();
        log.error("Serious exception is happened: " + generalErrorMessage, e);
        return "Произошла серьезная ошибка во время получения счетов: " + generalErrorMessage;
    }

    public String getAccount(Long chatId) {
        log.info("Getting account details from userID: {}", chatId);
        try {
            Optional<ResponseEntity<AccountListResponse[]>> response =
                    Optional.of(accountClient.getAccount(chatId));
            if (response.get().getStatusCode() == HttpStatus.NO_CONTENT) {
                log.warn("No accounts found for user");
                return "Нет счетов у пользователя";
            }
            return handleGetAccountResponse(response);
        } catch (HttpStatusCodeException e) {
            return handleGetAccountHttpStatusCodeException(e);
        } catch (Exception e) {
            return handleGetAccountGeneralException(e);
        }
    }

    private String handleMakeAccountTransferResponse(ResponseEntity<CreateTransferResponse> response) {
        HttpStatus currentStatus = response.getStatusCode();
        if (HttpStatus.OK == currentStatus && response.getBody() != null) {
            String transferId = response.getBody().transferId();
            log.error("Transfer was successfully done, ID: " + transferId);
            return "Перевод успешно выполнен, ID: " + transferId;
        } else {
            log.error("Cannot make transfer, status: " + currentStatus);
            return "Не могу совершить денежный перевод: " + currentStatus;
        }
    }

    private String handleMakeAccountTransferHttpStatusCodeException(HttpStatusCodeException e) {
        String responseErrorString = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        log.error("Cannot make funds transfer, HttpStatusCodeException: " + responseErrorString);
        return "Не могу выполнить денежный перевод, ошибка: " + responseErrorString;
    }

    private String handleMakeAccountTransferGeneralException(Exception e) {
        String generalErrorMessage = e.getMessage();
        log.error("Serious exception is happened while making funds transfer: " + generalErrorMessage, e);
        return "Произошла серьезная ошибка во время выполнения денежного перевода: " + generalErrorMessage;
    }

    public String makeAccountTransfer(@Valid CreateTransferRequest request) {
        try {
            log.info("Creating transfer from account1: {} to account2: {} with amount {}",
                    request.from(),
                    request.to(),
                    request.amount());
            return handleMakeAccountTransferResponse(accountClient.makeAccountTransfer(request));
        } catch (HttpStatusCodeException e) {
            return handleMakeAccountTransferHttpStatusCodeException(e);
        } catch (Exception e) {
            return handleMakeAccountTransferGeneralException(e);
        }
    }
}
