package ru.gpb.app.service;

import org.springframework.http.ResponseEntity;
import ru.gpb.app.dto.CreateAccountRequest;

public interface AccountClient {

    public ResponseEntity<Void> openAccount(CreateAccountRequest request);
}
