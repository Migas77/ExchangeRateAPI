package com.miguelbf.exchangerateapi.service;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.domain.record.RatesResponse;
import com.miguelbf.exchangerateapi.repository.IExchangeRatesRepository;
import com.miguelbf.exchangerateapi.service.impl.ExchangeRatesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRatesServiceMockRepoTest {

    @InjectMocks
    ExchangeRatesService exchangeRatesService;

    @Mock
    IExchangeRatesRepository exchangeRatesRepository;

    @Test
    void whenValidBaseCurrencyWithoutTarget_thenReturnAllRates() {
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, Map.of(
                Currency.EUR, new BigDecimal("0.85"),
                Currency.GBP, new BigDecimal("0.74"),
                Currency.JPY, new BigDecimal("158.74")
            )
        );
        when(exchangeRatesRepository.getLiveRates(Currency.USD, null)).thenReturn(liveRatesResponse);

        RatesResponse ratesResponse = exchangeRatesService.getRates(Currency.USD, null);

        assertEquals(ratesResponse.timestamp(), liveRatesResponse.timestamp());
        assertEquals(ratesResponse.source(), liveRatesResponse.source());
        assertEquals(ratesResponse.rates(), liveRatesResponse.quotes());
        verify(exchangeRatesRepository, times(1)).getLiveRates(Currency.USD, null);
    }

    @Test
    void whenValidBaseCurrencyWithTarget_thenReturnCorrectRate() {
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, Map.of(
                Currency.EUR, new BigDecimal("0.85")
            )
        );
        when(exchangeRatesRepository.getLiveRates(Currency.USD, Currency.EUR)).thenReturn(liveRatesResponse);

        RatesResponse ratesResponse = exchangeRatesService.getRates(Currency.USD, Currency.EUR);

        assertEquals(ratesResponse.timestamp(), liveRatesResponse.timestamp());
        assertEquals(ratesResponse.source(), liveRatesResponse.source());
        assertEquals(ratesResponse.rates(), liveRatesResponse.quotes());
        verify(exchangeRatesRepository, times(1)).getLiveRates(Currency.USD, Currency.EUR);
    }

}
