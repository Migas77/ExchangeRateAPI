package com.miguelbf.exchangerateapi.domain.clients.exchangerates;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miguelbf.exchangerateapi.domain.enums.Currency;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record LiveRatesResponse(
    @AssertTrue boolean success,
    @NotNull String terms,
    @NotNull String privacy,
    long timestamp,
    @NotNull Currency source,
    @NotEmpty Map<Currency, BigDecimal> quotes
) {

    @JsonCreator
    public static LiveRatesResponse of(
        @JsonProperty("success") boolean success,
        @JsonProperty("terms") String terms,
        @JsonProperty("privacy") String privacy,
        @JsonProperty("timestamp") long timestamp,
        @JsonProperty("source") Currency source,
        @JsonProperty("quotes") Map<String, BigDecimal> rawQuotes
    ) {
        String prefix = source.name();
        int prefixLength = prefix.length();

        Map<Currency, BigDecimal> quotes = HashMap.newHashMap(rawQuotes.size());
        rawQuotes.forEach((key, value) -> {
            if (!key.startsWith(prefix)) {
                throw new IllegalArgumentException(
                    "Quote key '%s' does not match source prefix '%s'".formatted(key, prefix)
                );
            }
            quotes.put(Currency.valueOf(key.substring(prefixLength)), value);
        });

        return new LiveRatesResponse(success, terms, privacy, timestamp, source, quotes);
    }

}
