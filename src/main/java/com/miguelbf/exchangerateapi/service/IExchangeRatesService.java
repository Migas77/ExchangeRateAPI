package com.miguelbf.exchangerateapi.service;

import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.domain.record.RatesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface IExchangeRatesService {

    @Valid RatesResponse getRates(@NotNull Currency from, Currency to);

}
