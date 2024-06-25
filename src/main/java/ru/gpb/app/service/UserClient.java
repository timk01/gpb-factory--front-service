package ru.gpb.app.service;

import org.springframework.http.ResponseEntity;
import ru.gpb.app.dto.CreateUserRequest;

public interface UserClient {

    public ResponseEntity<Void> register(CreateUserRequest request);
}
