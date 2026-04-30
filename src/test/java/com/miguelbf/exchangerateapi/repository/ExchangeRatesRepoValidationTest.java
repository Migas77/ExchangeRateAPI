package com.miguelbf.exchangerateapi.repository;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.repository.impl.ExchangeRatesRepository;
import com.miguelbf.exchangerateapi.service.impl.ExchangeRatesService;
import com.miguelbf.exchangerateapi.utilities.JsonUtils;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 *  Tests validation constraints (annotations) in {@link IExchangeRatesRepository}.
 *
 *  <p>
 *      Verifies that the repository enforces bean validation constraints on both input parameters and the response.
 *      Domain validation for the actual used payload annotations lives in separate test class {@link }
 *  </p>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "spring.main.allow-bean-definition-overriding=true"
)
public class ExchangeRatesRepoValidationTest {

    @MockitoSpyBean
    ExchangeRatesRepository exchangeRatesRepository;

    @Test
    void whenNullBaseCurrency_thenThrowConstraintViolation() {
        assertThrows(ConstraintViolationException.class, () ->
            exchangeRatesRepository.getLiveRates(null, Currency.EUR)
        );
    }

    @Test
    void whenInvalidRatesRatesResponse_thenThrowConstraintViolation() {
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, null
        );

        doReturn(liveRatesResponse)
            .when(exchangeRatesRepository)
            .getLiveRates(Currency.USD, Currency.EUR);

        assertThrows(ConstraintViolationException.class, () ->
            exchangeRatesRepository.getLiveRates(Currency.USD, Currency.EUR)
        );
    }

}
