package com.miguelbf.exchangerateapi.domain.clients.exchangerates;

import jakarta.validation.constraints.NotNull;

public record ErrorResponse(
    boolean success,
    @NotNull ErrorStatus error
) {}
