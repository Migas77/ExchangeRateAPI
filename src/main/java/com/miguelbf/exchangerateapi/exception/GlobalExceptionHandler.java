package com.miguelbf.exchangerateapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleResourceAccessException(
        ResourceAccessException ex, HttpServletRequest request
    ) {
        String detail;
        HttpStatus httpStatus;
        Throwable cause = ex.getCause();
        if (cause instanceof SocketTimeoutException) {
            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
            detail = "The upstream service did not respond in time. Please try again later.";
            log.atWarn()
                .setMessage("Upstream timeout")
                .addKeyValue("reason", cause.getMessage())
                .addKeyValue("exception", cause.getClass().getSimpleName())
                .log();
        } else if (
            cause instanceof ConnectException
            || cause instanceof UnknownHostException
            || cause instanceof NoRouteToHostException
        ){
            httpStatus = HttpStatus.BAD_GATEWAY;
            detail = "The upstream service is currently unreachable. Please try again later.";
            log.atWarn()
                .setMessage("Upstream unreachable")
                .addKeyValue("reason", cause.getMessage())
                .addKeyValue("exception", cause.getClass().getSimpleName())
                .log();
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            detail = "An unexpected error occurred. Please try again later.";
            log.atError().setMessage("Unexpected ResourceAccessException").setCause(ex).log();
        }
        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setDetail(detail);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(RatesUpstreamAPIException.BadGateway.class)
    public ProblemDetail handleRatesUpstreamBadGatewayException(
        RatesUpstreamAPIException.BadGateway ex, HttpServletRequest request
    ){
        log.atError()
            .setMessage("Upstream unrecognizable response")
            .addKeyValue("upstreamStatus", ex.getResponseStatusCode().toString())
            .addKeyValue("upstreamSuccess", ex.isSuccess())
            .addKeyValue("upstreamCode", ex.getCode())
            .addKeyValue("upstreamMessage", ex.getMessage())
            .log();
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setDetail("The upstream service returned an unrecognizable response");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(RatesUpstreamAPIException.HttpError.class)
    public ProblemDetail handleRatesUpstreamHttpErrorException(
        RatesUpstreamAPIException.HttpError ex, HttpServletRequest request
    ){
        log.atWarn()
            .setMessage("Upstream error response")
            .addKeyValue("upstreamStatus", ex.getResponseStatusCode().toString())
            .addKeyValue("upstreamSuccess", ex.isSuccess())
            .addKeyValue("upstreamCode", ex.getCode())
            .addKeyValue("upstreamMessage", ex.getMessage())
            .log();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setDetail("An unexpected error occurred processing upstream. Please try again later.");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        // Safeline method - Catch-all handler which prevents unhandled exceptions from leaking stack traces
        // although spring.web.error.include-stacktrace=never is set in application.properties
        log.atError().setMessage("Unhandled exception").setCause(ex).log();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setDetail("An unexpected error occurred. Please try again later.");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

}
