package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class RandomService {
    private static final Logger logger = LoggerFactory.getLogger(RandomService.class);

    private final RestClient restClient;

    public String getRandomStatus(Payment payment) {
        try {
            String body = restClient.get()
                    .uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")
                    .retrieve()
                    .body(String.class);

            if (body != null && !body.isBlank()) {
                int randomNumber = Integer.parseInt(body.trim());
                logger.info("Received random number: {}", randomNumber);
                payment.setStatus(randomNumber % 2 == 0 ? "SUCCESS" : "FAILED");
            } else {
                throw new RuntimeException("Empty or invalid response from API");
            }
        } catch (RestClientException e) {
            logger.error("Error calling random API: {}", e.getMessage(), e);
            payment.setStatus("FAILED");
        }
        return payment.getStatus();
    }
}