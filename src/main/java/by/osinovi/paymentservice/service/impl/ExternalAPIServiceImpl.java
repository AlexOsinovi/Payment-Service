package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.service.ExternalAPIService;
import by.osinovi.paymentservice.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalAPIServiceImpl implements ExternalAPIService {

    private final RestClient restClient;

    @Override
    public PaymentStatus getStatus() {
        try {
            String body = restClient.get()
                    .uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")
                    .retrieve()
                    .body(String.class);

            if (body != null && !body.isBlank()) {
                try {
                    int randomNumber = Integer.parseInt(body.trim());
                    log.info("Received random number: {}", randomNumber);
                    if (randomNumber < 0) {
                        log.error("Invalid random number: {}", randomNumber);
                        throw new IllegalArgumentException("Random number cannot be negative");
                    }
                    return randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
                } catch (NumberFormatException e) {
                    log.error("Failed to parse response as integer: {}", body, e);
                    throw new RuntimeException("Invalid integer response from API", e);
                }
            } else {
                log.error("Empty or invalid response from API");
                throw new RuntimeException("Empty or invalid response from API");
            }
        } catch (RestClientException e) {
            log.error("Error calling random API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call random API", e);
        }
    }
}