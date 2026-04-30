package com.miguelbf.exchangerateapi.utilities;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.LiveRatesResponse;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonUtils {

    public static String toJson(ObjectMapper objectMapper, Object object) {
        return objectMapper.writeValueAsString(object);
    }

    /**
     *
     * @param serializedResponse representing the expected LiveRatesResponse after serialization
     * @return a JSON string representing the given LiveRatesResponse, as it would be returned by the upstream API
     */
    public static String getRawLiveRatesResponse(ObjectMapper objectMapper, LiveRatesResponse serializedResponse){
        Map<String, BigDecimal> rawQuotes = null;

        if (serializedResponse.quotes() != null) {
            rawQuotes = serializedResponse.quotes().entrySet().stream()
                .collect(Collectors.toMap(
                    e -> serializedResponse.source().name() + e.getKey().name(),
                    Map.Entry::getValue
                ));
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", serializedResponse.success());
        responseMap.put("terms", serializedResponse.terms());
        responseMap.put("privacy", serializedResponse.privacy());
        responseMap.put("timestamp", serializedResponse.timestamp());
        responseMap.put("source", serializedResponse.source().name());
        responseMap.put("quotes", rawQuotes);

        return objectMapper.writeValueAsString(responseMap);
    }
}
