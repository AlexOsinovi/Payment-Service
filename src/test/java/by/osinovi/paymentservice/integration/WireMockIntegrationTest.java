package by.osinovi.paymentservice.integration;

import by.osinovi.paymentservice.service.impl.ExternalAPIServiceImpl;
import by.osinovi.paymentservice.util.PaymentStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WireMockIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private ExternalAPIServiceImpl externalAPIService;

    static {
        wireMockServer = new WireMockServer(wireMockConfig().port(8088));
        wireMockServer.start();
        System.out.println("WireMock started on port: 8088");
        WireMock.configureFor("localhost", 8088);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("random-api-url", () -> "http://localhost:8088");
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }}

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithEvenNumber() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("42")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithOddNumber() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("43")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithEmptyBody() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("")));

        assertThrows(RuntimeException.class, () -> externalAPIService.getStatus());
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithBlankBody() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("   ")));

        assertThrows(RuntimeException.class, () -> externalAPIService.getStatus());
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithNonNumericValue() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("not-a-number")));

        assertThrows(NumberFormatException.class, () -> externalAPIService.getStatus());
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWith500Error() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Internal Server Error")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWith404Error() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Not Found")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldHandleResponseWithWhitespace_WhenValidNumber() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("  50  ")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithMinimumValue() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("2")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithMaximumValue() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("99")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithMaximumEvenValue() {
        stubFor(get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("100")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")));
    }
}