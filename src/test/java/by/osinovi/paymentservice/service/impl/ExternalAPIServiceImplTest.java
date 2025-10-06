package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.util.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAPIServiceImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private ExternalAPIServiceImpl externalAPIService;

    @BeforeEach
    void setUp() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenRandomNumberIsEven() {
        when(responseSpec.body(String.class)).thenReturn("42");

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenRandomNumberIsOdd() {
        when(responseSpec.body(String.class)).thenReturn("43");

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenResponseIsNull() {
        when(responseSpec.body(String.class)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> externalAPIService.getStatus());
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenResponseIsBlank() {
        when(responseSpec.body(String.class)).thenReturn("   ");

        assertThrows(RuntimeException.class, () -> externalAPIService.getStatus());
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenResponseIsEmpty() {
        when(responseSpec.body(String.class)).thenReturn("");

        assertThrows(RuntimeException.class, () -> externalAPIService.getStatus());
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenResponseContainsNonNumericValue() {
        when(responseSpec.body(String.class)).thenReturn("not-a-number");

        assertThrows(NumberFormatException.class, () -> externalAPIService.getStatus());
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenRestClientThrowsException() {
        when(responseSpec.body(String.class)).thenThrow(new RestClientException("Connection failed"));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldHandleResponseWithWhitespace_WhenValidNumber() {
        when(responseSpec.body(String.class)).thenReturn("  50  ");

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(restClient, times(1)).get();
        verify(requestHeadersUriSpec, times(1)).uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
        verify(requestHeadersSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(String.class);
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenRandomNumberIsOne() {
        when(responseSpec.body(String.class)).thenReturn("1");

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenRandomNumberIsHundred() {
        when(responseSpec.body(String.class)).thenReturn("100");

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
    }
}
