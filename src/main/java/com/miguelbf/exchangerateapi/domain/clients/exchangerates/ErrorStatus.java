package com.miguelbf.exchangerateapi.domain.clients.exchangerates;

import jakarta.validation.constraints.NotNull;

public record ErrorStatus(
    int code,
    @NotNull String info
) {}
