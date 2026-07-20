package com.miguelbf.exchangerateapi.exception;

import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public abstract class RatesUpstreamAPIException extends RuntimeException {

    private final HttpStatusCode responseStatusCode;
    private final boolean success;
    private final int code;

    protected RatesUpstreamAPIException(HttpStatusCode responseStatusCode, ErrorResponse errorResponse) {
        super(errorResponse.error().info());
        this.responseStatusCode = responseStatusCode;
        this.success = errorResponse.success();
        this.code = errorResponse.error().code();
    }

    public static class BadGateway extends RatesUpstreamAPIException {
        public BadGateway(HttpStatusCode responseStatusCode, ErrorResponse errorResponse) {
            super(responseStatusCode, errorResponse);
        }
    }

    public static class HttpError extends RatesUpstreamAPIException {
        public HttpError(HttpStatusCode responseStatusCode, ErrorResponse errorResponse) {
            super(responseStatusCode, errorResponse);
        }
    }

}
