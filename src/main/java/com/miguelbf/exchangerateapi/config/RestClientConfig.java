package com.miguelbf.exchangerateapi.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Validated
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient getExchangeRatesRestClient(
        @Value("${app.clients.exchange-rates.base-url}") @NotBlank @URL String baseUrl,
        @Value("${app.clients.exchange-rates.access-key}")
            @Pattern(regexp = "^[a-z0-9]{32}$") @NotBlank String accessKey,
        @Value("${app.clients.exchange-rates.timeout-seconds:5}") int timeoutSeconds
    ) {
        return RestClient.builder()
            .uriBuilderFactory(createAccessKeyUriBuilderFactory(baseUrl, accessKey))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    private static DefaultUriBuilderFactory createAccessKeyUriBuilderFactory(String baseUrl, String accessKey) {
        return new DefaultUriBuilderFactory(baseUrl) {

            private URI appendAccessKey(URI uri) {
                return UriComponentsBuilder.fromUri(uri)
                    .replaceQueryParam("access_key", accessKey)
                    .build(true)
                    .toUri();
            }

            @Override
            public @NonNull URI expand(@NonNull String uriTemplate, @Nullable Object @NonNull ... uriVariables) {
                return appendAccessKey(super.expand(uriTemplate, uriVariables));
            }

            @Override
            public @NonNull URI expand(@NonNull String uriTemplate, @NonNull Map<String, ?> uriVariables) {
                return appendAccessKey(super.expand(uriTemplate, uriVariables));
            }

            @Override
            public @NonNull UriBuilder uriString(@NonNull String uriTemplate) {
                return super.uriString(uriTemplate).queryParam("access_key", accessKey);
            }

            @Override
            public @NonNull UriBuilder builder() {
                return super.builder().queryParam("access_key", accessKey);
            }
        };
    }

}
