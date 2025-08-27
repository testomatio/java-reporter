package io.testomat.core.client.http.retryable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.testomat.core.exception.RequestExecutionFailedException;
import io.testomat.core.exception.RequestStatusNotSuccessException;
import io.testomat.core.exception.RequestTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NativeRetryableRequestExecutor Tests")
class NativeRetryableRequestExecutorTest {

    @Mock
    private HttpClient mockClient;
    
    @Mock
    private HttpRequest mockRequest;
    
    @Mock 
    private HttpResponse<String> mockResponse;

    private NativeRetryableRequestExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new NativeRetryableRequestExecutor();
    }

    @Test
    @DisplayName("Should return response on successful request")
    void executeRetryable_SuccessfulRequest_ShouldReturnResponse() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(mockResponse);

        HttpResponse<String> result = executor.executeRetryable(mockRequest, mockClient);

        assertSame(mockResponse, result);
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on timeout exception")
    void executeRetryable_HttpTimeoutException_ShouldRetryAndThrowTimeout() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new HttpTimeoutException("Timeout"))
                .thenThrow(new HttpTimeoutException("Timeout"))
                .thenThrow(new HttpTimeoutException("Timeout"));

        RequestTimeoutException exception = assertThrows(
                RequestTimeoutException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Request timeout"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on connect exception")
    void executeRetryable_ConnectException_ShouldRetryAndThrowExecutionFailed() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new ConnectException("Connection failed"))
                .thenThrow(new ConnectException("Connection failed"))
                .thenThrow(new ConnectException("Connection failed"));

        RequestExecutionFailedException exception = assertThrows(
                RequestExecutionFailedException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Network error occurred"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on socket timeout exception")
    void executeRetryable_SocketTimeoutException_ShouldRetryAndThrowExecutionFailed() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new SocketTimeoutException("Socket timeout"))
                .thenThrow(new SocketTimeoutException("Socket timeout"))
                .thenThrow(new SocketTimeoutException("Socket timeout"));

        RequestExecutionFailedException exception = assertThrows(
                RequestExecutionFailedException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Network error occurred"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on 502 status code")
    void executeRetryable_Status502_ShouldRetryAndThrowStatusException() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(502);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(mockResponse);

        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("status code 502"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on 503 status code")
    void executeRetryable_Status503_ShouldRetryAndThrowStatusException() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(503);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(mockResponse);

        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("status code 503"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should retry on 504 status code")
    void executeRetryable_Status504_ShouldRetryAndThrowStatusException() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(504);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(mockResponse);

        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("status code 504"));
        verify(mockClient, times(3)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should not retry on 400 status code")
    void executeRetryable_Status400_ShouldNotRetry() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(mockResponse);

        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("status code 400"));
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should not retry on 404 status code")
    void executeRetryable_Status404_ShouldNotRetry() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenReturn(mockResponse);

        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("status code 404"));
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should succeed after retry")
    void executeRetryable_SucceedAfterRetry_ShouldReturnResponse() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new ConnectException("Connection failed"))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        HttpResponse<String> result = executor.executeRetryable(mockRequest, mockClient);

        assertSame(mockResponse, result);
        verify(mockClient, times(2)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should handle interruption during retry")
    void executeRetryable_InterruptedDuringRetry_ShouldThrowExecutionFailed() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new InterruptedException("Interrupted"));

        RequestExecutionFailedException exception = assertThrows(
                RequestExecutionFailedException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Request was interrupted"));
        assertTrue(Thread.currentThread().isInterrupted());
        // Reset interrupt status
        Thread.interrupted();
    }

    @Test
    @DisplayName("Should not retry generic IOException")
    void executeRetryable_GenericIOException_ShouldNotRetry() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new IOException("Generic IO error"));

        RequestExecutionFailedException exception = assertThrows(
                RequestExecutionFailedException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Network error occurred"));
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should handle 2xx success status codes")
    void executeRetryable_Status201_ShouldReturnResponse() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(mockResponse);

        HttpResponse<String> result = executor.executeRetryable(mockRequest, mockClient);

        assertSame(mockResponse, result);
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should handle 2xx edge cases")
    void executeRetryable_Status299_ShouldReturnResponse() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(299);
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString())).thenReturn(mockResponse);

        HttpResponse<String> result = executor.executeRetryable(mockRequest, mockClient);

        assertSame(mockResponse, result);
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Should handle other runtime exceptions")
    void executeRetryable_RuntimeException_ShouldThrowExecutionFailed() throws IOException, InterruptedException {
        when(mockClient.send(mockRequest, HttpResponse.BodyHandlers.ofString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        RequestExecutionFailedException exception = assertThrows(
                RequestExecutionFailedException.class,
                () -> executor.executeRetryable(mockRequest, mockClient)
        );

        assertTrue(exception.getMessage().contains("Request failed"));
        verify(mockClient, times(1)).send(mockRequest, HttpResponse.BodyHandlers.ofString());
    }
}