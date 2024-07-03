package ru.gpb.app.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public record CreateTransferRequestDto(String from, @NotNull Long firstUserId, String to, @Positive String amount) {
}
