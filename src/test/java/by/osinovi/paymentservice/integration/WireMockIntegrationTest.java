package by.osinovi.paymentservice.integration;

import by.osinovi.paymentservice.service.impl.ExternalAPIServiceImpl;
import by.osinovi.paymentservice.util.PaymentStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WireMockIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private ExternalAPIServiceImpl externalAPIService;

    @BeforeAll
    static void setUpClass() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        System.out.println("WireMock started on port: " + wireMockServer.port());
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("random-api-url", () -> "http://localhost:" + wireMockServer.port() + "/services/v1/random_number1/100");
    }

    @AfterAll
    static void tearDownClass() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("WireMock stopped");
        }
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithEvenNumber() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[42]")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithOddNumber() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[43]")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWith500Error() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal Server Error\"}")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWith404Error() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Not Found\"}")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldHandleResponseWithWhitespace_WhenValidNumber() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("  [50]  ")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithMinimumValue() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[2]")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnFailed_WhenAPIRespondsWithMaximumValue() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[99]")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.FAILED, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }

    @Test
    void getStatus_ShouldReturnSuccess_WhenAPIRespondsWithMaximumEvenValue() {
        stubFor(get(urlEqualTo("/services/v1/random_number1/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[100]")));

        PaymentStatus result = externalAPIService.getStatus();

        assertEquals(PaymentStatus.SUCCESS, result);
        verify(getRequestedFor(urlEqualTo("/services/v1/random_number1/100")));
    }
}