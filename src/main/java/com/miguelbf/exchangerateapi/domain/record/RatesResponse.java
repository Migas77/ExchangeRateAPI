package com.miguelbf.exchangerateapi.domain.record;

import com.miguelbf.exchangerateapi.domain.enums.Currency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record RatesResponse (
    @NotNull long timestamp,
    @NotNull Currency source,
    @NotEmpty Map<Currency, BigDecimal> rates
) {}
