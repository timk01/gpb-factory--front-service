package ru.gpb.app.dto;

import javax.validation.constraints.Positive;

public record CreateTransferRequest(String from, String to, @Positive String amount) {
}
