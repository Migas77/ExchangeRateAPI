package com.miguelbf.exchangerateapi.service.impl;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.domain.record.RatesResponse;
import com.miguelbf.exchangerateapi.repository.IExchangeRatesRepository;
import com.miguelbf.exchangerateapi.service.IExchangeRatesService;
import org.springframework.stereotype.Service;


@Service
public class ExchangeRatesService implements IExchangeRatesService {

    private final IExchangeRatesRepository exchangeRatesRepository;

    public ExchangeRatesService(IExchangeRatesRepository exchangeRatesRepository) {
        this.exchangeRatesRepository = exchangeRatesRepository;
    }

    @Override
    public RatesResponse getRates(Currency from, Currency to) {
        LiveRatesResponse liveRatesResponse = this.exchangeRatesRepository.getLiveRates(from, to);
        return new RatesResponse(
            liveRatesResponse.timestamp(),
            liveRatesResponse.source(),
            liveRatesResponse.quotes()
        );
    }
}
