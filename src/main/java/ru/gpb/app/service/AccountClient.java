package ru.gpb.app.service;

import org.springframework.http.ResponseEntity;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;

import javax.validation.Valid;

public interface AccountClient {

    public ResponseEntity<Void> openAccount(CreateAccountRequest request);

    public  ResponseEntity<AccountListResponse[]> getAccount(Long chatId);

    public ResponseEntity<CreateTransferResponse> makeAccountTransfer(@Valid CreateTransferRequestDto request);
}
