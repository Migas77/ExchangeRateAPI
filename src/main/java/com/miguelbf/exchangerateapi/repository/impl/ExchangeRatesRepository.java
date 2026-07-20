package com.miguelbf.exchangerateapi.repository.impl;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorResponse;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import com.miguelbf.exchangerateapi.exception.RatesUpstreamAPIException;
import com.miguelbf.exchangerateapi.repository.IExchangeRatesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
public class ExchangeRatesRepository implements IExchangeRatesRepository {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ExchangeRatesRepository(
        @Qualifier("getExchangeRatesRestClient") RestClient restClient,
        ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LiveRatesResponse getLiveRates(Currency from, Currency to) {
        // API docs are incorrect and API returns 200 even when there is an error (e.g. missing access key)
        // Throw HttpErrorException (from RatesUpstreamAPIException) when status code is error (API docs respected), or
        // if the API docs are not respected, use the success parameter from response body and throw BadGatewayException
        return restClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/live")
                .queryParam("source", from.name())
                .queryParamIfPresent("currencies", Optional.ofNullable(to).map(Currency::name))
                .build()
            )
            .exchange((request, response) -> {
                HttpStatusCode statusCode = response.getStatusCode();
                JsonNode root = objectMapper.readTree(response.getBody());

                if (statusCode.isError()) {
                    ErrorResponse errorResponse = objectMapper.treeToValue(root, ErrorResponse.class);
                    throw new RatesUpstreamAPIException.HttpError(statusCode, errorResponse);
                }

                if (root.has("success") && !root.get("success").asBoolean()){
                    ErrorResponse errorResponse = objectMapper.treeToValue(root, ErrorResponse.class);
                    throw new RatesUpstreamAPIException.BadGateway(statusCode, errorResponse);
                }

                return objectMapper.treeToValue(root, LiveRatesResponse.class);
            });
    }
}
// TODO: object mapper not enforcing validation