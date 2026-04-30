package com.miguelbf.exchangerateapi.repository;

import com.miguelbf.exchangerateapi.config.RestClientConfig;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorResponse;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorStatus;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.exception.RatesUpstreamAPIException;
import com.miguelbf.exchangerateapi.repository.impl.ExchangeRatesRepository;
import com.miguelbf.exchangerateapi.utilities.JsonUtils;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(
    components = ExchangeRatesRepository.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {RestClientConfig.class}
    )
)
public class ExchangeRatesRepoMockUpstreamTest {

    @Autowired
    private ExchangeRatesRepository exchangeRatesRepository;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNoTarget_whenGetLiveRates_thenCurrenciesParamIsAbsentAndSuccess() {
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, Map.of(
            Currency.EUR, new BigDecimal("0.85"),
            Currency.GBP, new BigDecimal("0.74"),
            Currency.JPY, new BigDecimal("158.74")
        ));

        server.expect(once(), requestTo(containsString("/live")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("access_key", "test0access0key0with32characters"))
            .andExpect(queryParam("source", liveRatesResponse.source().name()))
            .andExpect(request -> assertThat(request.getURI().getQuery()).doesNotContain("currencies"))
            .andRespond(withSuccess(
                JsonUtils.getRawLiveRatesResponse(objectMapper, liveRatesResponse),
                MediaType.APPLICATION_JSON
            ));

        LiveRatesResponse result = exchangeRatesRepository.getLiveRates(liveRatesResponse.source(), null);

        assertEquals(liveRatesResponse, result);
        server.verify();
    }

    @Test
    void givenSuppliedTarget_whenGetLiveRates_thenCurrenciesParamIsPresentAndSuccess() {
        Currency targetCurr = Currency.EUR;
        LiveRatesResponse liveRatesResponse = new LiveRatesResponse(
            true,"terms", "privacy", 1L, Currency.USD, Map.of(
            targetCurr, new BigDecimal("0.85")
        ));

        server.expect(once(), requestTo(containsString("/live")))
            .andExpect(method(HttpMethod.GET))
            .andExpect(queryParam("access_key", "test0access0key0with32characters"))
            .andExpect(queryParam("source", liveRatesResponse.source().name()))
            .andExpect(queryParam("currencies", targetCurr.name()))
            .andRespond(withSuccess(
                JsonUtils.getRawLiveRatesResponse(objectMapper, liveRatesResponse),
                MediaType.APPLICATION_JSON
            ));

        LiveRatesResponse result = exchangeRatesRepository.getLiveRates(liveRatesResponse.source(), targetCurr);

        assertEquals(liveRatesResponse, result);
        server.verify();
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void whenUnsuccessfulRequest_thenThrowsRatesUpstreamAPIHttpErrorException(
        HttpStatus status, int code, String info
    ) {
        ErrorResponse errorResponse = new ErrorResponse(false, new ErrorStatus(code, info));
        String jsonErrorResponse = JsonUtils.toJson(objectMapper, errorResponse);

        server.expect(once(), requestTo(containsString("/live")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(status).body(jsonErrorResponse).contentType(MediaType.APPLICATION_JSON));

        RatesUpstreamAPIException.HttpError exception = assertThrows(RatesUpstreamAPIException.HttpError.class, () ->
            exchangeRatesRepository.getLiveRates(Currency.USD, Currency.EUR)
        );
        assertFalse(exception.isSuccess());
        assertEquals(status, exception.getResponseStatusCode());
        assertEquals(code, exception.getCode());
        assertEquals(info, exception.getMessage());
        server.verify();
    }

    @ParameterizedTest
    @MethodSource("errorScenarios")
    void whenUnsuccessfulRequestWithSucessStatusCode_thenThrowsRatesUpstreamAPIBadGatewayException(
        HttpStatus status, int code, String info
    ) {
        // Test method because of observed inconsistency between response status code (200)
        // and the success field (false) in same response
        ErrorResponse errorResponse = new ErrorResponse(false, new ErrorStatus(code, info));
        String jsonErrorResponse = JsonUtils.toJson(objectMapper, errorResponse);

        server.expect(once(), requestTo(containsString("/live")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess().body(jsonErrorResponse).contentType(MediaType.APPLICATION_JSON));

        RatesUpstreamAPIException.BadGateway exception = assertThrows(RatesUpstreamAPIException.BadGateway.class, () ->
            exchangeRatesRepository.getLiveRates(Currency.USD, Currency.EUR)
        );
        assertFalse(exception.isSuccess());
        assertEquals(HttpStatus.OK, exception.getResponseStatusCode());
        assertEquals(code, exception.getCode());
        assertEquals(info, exception.getMessage());
        server.verify();
    }

    private static Stream<Arguments> errorScenarios() {
        return Stream.of(
            Arguments.of(
                HttpStatus.BAD_REQUEST,
                301,
                "User did not specify a date. [historical]"
            ),
            Arguments.of(
                HttpStatus.UNAUTHORIZED,
                101,
                "User did not supply an access key or supplied an invalid access key."
            ),
            Arguments.of(
                HttpStatus.TOO_MANY_REQUESTS,
                104,
                "Your monthly usage limit has been reached. Please upgrade your subscription plan."
            ),
            Arguments.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                500,
                "Internal server error."
            )
        );
    }

}
