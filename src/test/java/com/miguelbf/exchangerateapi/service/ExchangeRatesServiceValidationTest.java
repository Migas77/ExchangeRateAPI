package com.miguelbf.exchangerateapi.service;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.repository.IExchangeRatesRepository;
import com.miguelbf.exchangerateapi.service.impl.ExchangeRatesService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


/**
 *  Tests validation constraints (annotations) in {@link IExchangeRatesService}.
 *
 *  <p>
 *      Verifies that the service enforces bean validation constraints on both input parameters and the response.
 *      Domain validation for the actual used payload annotations lives in separate test class {@link }
 *  </p>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "spring.main.allow-bean-definition-overriding=true"
)
public class ExchangeRatesServiceValidationTest {

    @Autowired
    ExchangeRatesService exchangeRatesService;

    @MockitoBean
    IExchangeRatesRepository exchangeRatesRepository;

    @Test
    void whenNullBaseCurrency_thenThrowConstraintViolation() {
        assertThrows(ConstraintViolationException.class, () ->
            exchangeRatesService.getRates(null, Currency.EUR)
        );
        verify(exchangeRatesRepository, never()).getLiveRates(null, Currency.EUR);
    }

    @Test
    void whenInvalidRatesRatesResponse_thenThrowConstraintViolation() {
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, null
        );
        when(exchangeRatesRepository.getLiveRates(Currency.USD, Currency.EUR)).thenReturn(liveRatesResponse);

        assertThrows(ConstraintViolationException.class, () ->
            exchangeRatesService.getRates(Currency.USD, Currency.EUR)
        );
        verify(exchangeRatesRepository, times(1)).getLiveRates(Currency.USD, Currency.EUR);
    }
}