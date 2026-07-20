package com.miguelbf.exchangerateapi.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorResponse;
import com.miguelbf.exchangerateapi.domain.clients.exchangerates.ErrorStatus;
import com.miguelbf.exchangerateapi.stubs.StubController;
import com.miguelbf.exchangerateapi.stubs.StubService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.slf4j.event.KeyValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StubController.class)
public class GlobalExceptionHandlerMockExceptionsTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StubService stubService;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        logger.detachAppender(logAppender);
    }

    @Test
    void whenControllerLoaded_thenGlobalExceptionHandlerIsPresent() {
        assertDoesNotThrow(() -> context.getBean(GlobalExceptionHandler.class));
    }

    @Test
    void whenUnexpectedException_thenStatusInternalServerError() throws Exception {
        String detailMessage = "An unexpected error occurred. Please try again later.";
        Exception exception = new RuntimeException("Unexpected error");
        doThrow(exception).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Internal Server Error")))
            .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Unhandled exception", event.getMessage());
        assertEquals(exception.getClass().getName(), event.getThrowableProxy().getClassName());
        assertEquals(exception.getMessage(), event.getThrowableProxy().getMessage());
        verify(stubService, times(1)).call();
    }

    @Test
    void whenSocketTimeoutException_thenGatewayTimeout() throws Exception {
        String detailMessage = "The upstream service did not respond in time. Please try again later.";
        SocketTimeoutException cause = new SocketTimeoutException("read timed out");
        doThrow(new ResourceAccessException("timeout", cause)).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isGatewayTimeout())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Gateway Timeout")))
            .andExpect(jsonPath("$.status", is(HttpStatus.GATEWAY_TIMEOUT.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.WARN, event.getLevel());
        assertEquals("Upstream timeout", event.getMessage());
        assertThat(event.getKeyValuePairs(), hasItems(
            new KeyValuePair("reason", cause.getMessage()),
            new KeyValuePair("exception", cause.getClass().getSimpleName())
        ));
        verify(stubService, times(1)).call();
    }

    @ParameterizedTest
    @MethodSource("unreachableCauses")
    void whenUnreachableException_thenBadGateway(IOException cause) throws Exception {
        String detailMessage = "The upstream service is currently unreachable. Please try again later.";
        doThrow(new ResourceAccessException("unreachable", cause)).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Bad Gateway")))
            .andExpect(jsonPath("$.status", is(HttpStatus.BAD_GATEWAY.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.WARN, event.getLevel());
        assertEquals("Upstream unreachable", event.getMessage());
        assertThat(event.getKeyValuePairs(), hasItems(
            new KeyValuePair("reason", cause.getMessage()),
            new KeyValuePair("exception", cause.getClass().getSimpleName())
        ));
        verify(stubService, times(1)).call();
    }

    @Test
    void whenResourceAccessExceptionUnknownCause_thenInternalServerError() throws Exception {
        String detailMessage = "An unexpected error occurred. Please try again later.";
        IOException cause = new IOException("some io error");
        ResourceAccessException exception = new ResourceAccessException("unknown", cause);
        doThrow(exception).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Internal Server Error")))
            .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Unexpected ResourceAccessException", event.getMessage());
        assertEquals(exception.getClass().getName(), event.getThrowableProxy().getClassName());
        assertEquals(exception.getMessage(), event.getThrowableProxy().getMessage());
        verify(stubService, times(1)).call();
    }

    @Test
    void whenRatesUpstreamBadGatewayException_thenBadGateway() throws Exception {
        String detailMessage = "The upstream service returned an unrecognizable response";
        ErrorResponse errorResponse = new ErrorResponse(true, new ErrorStatus(104, "bad gateway info"));
        RatesUpstreamAPIException exception = new RatesUpstreamAPIException.BadGateway(HttpStatus.BAD_GATEWAY, errorResponse);
        doThrow(exception).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Bad Gateway")))
            .andExpect(jsonPath("$.status", is(HttpStatus.BAD_GATEWAY.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Upstream unrecognizable response", event.getMessage());
        assertThat(event.getKeyValuePairs(), hasItems(
            new KeyValuePair("upstreamStatus", exception.getResponseStatusCode().toString()),
            new KeyValuePair("upstreamSuccess", exception.isSuccess()),
            new KeyValuePair("upstreamCode", exception.getCode()),
            new KeyValuePair("upstreamMessage", exception.getMessage())
        ));
        verify(stubService, times(1)).call();
    }

    @Test
    void whenRatesUpstreamHttpErrorException_thenInternalServerError() throws Exception {
        String detailMessage = "An unexpected error occurred processing upstream. Please try again later.";
        ErrorResponse errorResponse = new ErrorResponse(false, new ErrorStatus(104, "http error info"));
        RatesUpstreamAPIException.HttpError exception = new RatesUpstreamAPIException.HttpError(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
        doThrow(exception).when(stubService).call();

        mockMvc
            .perform(get("/stub"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.instance", is("/stub")))
            .andExpect(jsonPath("$.title", is("Internal Server Error")))
            .andExpect(jsonPath("$.status", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
            .andExpect(jsonPath("$.detail", is(detailMessage)));

        assertEquals(1, logAppender.list.size(), "Expected exactly one log event");
        ILoggingEvent event = logAppender.list.getFirst();
        assertEquals(Level.WARN, event.getLevel());
        assertEquals("Upstream error response", event.getMessage());
        assertThat(event.getKeyValuePairs(), hasItems(
            new KeyValuePair("upstreamStatus", exception.getResponseStatusCode().toString()),
            new KeyValuePair("upstreamSuccess", exception.isSuccess()),
            new KeyValuePair("upstreamCode", exception.getCode()),
            new KeyValuePair("upstreamMessage", exception.getMessage())
        ));
        verify(stubService, times(1)).call();
    }


    private static Stream<Arguments> unreachableCauses() {
        return Stream.of(
            Arguments.of(new ConnectException("connection refused")),
            Arguments.of(new UnknownHostException("unknown host")),
            Arguments.of(new NoRouteToHostException("no route"))
        );
    }

}
