package com.miguelbf.exchangerateapi.exception;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorResponse;
import org.springframework.http.HttpStatusCode;

public class ExchangeRatesApiException extends RuntimeException {

    private final HttpStatusCode responseStatusCode;
    private final boolean success;
    private final int code;

    public ExchangeRatesApiException(HttpStatusCode responseStatusCode, ErrorResponse errorResponse) {
        super(errorResponse.error().info());
        this.responseStatusCode = responseStatusCode;
        this.success = errorResponse.success();
        this.code = errorResponse.error().code();
    }

}
// TODO: Exception Handlers (@RestControllerAdvice)
// ExchangeRatesApiException
// ValueInstantiationException
// InvalidFormatException
// jakarta.validation.ConstraintViolationException