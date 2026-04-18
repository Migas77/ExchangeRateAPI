package com.miguelbf.exchangerateapi.repository;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface IExchangeRatesRepository {

    @Valid LiveRatesResponse getLiveRates(@NotNull Currency from, Currency to);

}
