package ru.gpb.app.dto;

public record CreateAccountRequest(long userId, String userName, String accountName) {
}
