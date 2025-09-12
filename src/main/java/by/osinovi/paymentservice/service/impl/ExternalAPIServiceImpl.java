package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.service.ExternalAPIService;
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

    public String getStatus() {
        try {
            String body = restClient.get()
                    .uri("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new")
                    .retrieve()
                    .body(String.class);

            if (body != null && !body.isBlank()) {
                int randomNumber = Integer.parseInt(body.trim());
                log.info("Received random number: {}", randomNumber);
                return randomNumber % 2 == 0 ? "SUCCESS" : "FAILED";
            } else {
                throw new RuntimeException("Empty or invalid response from API");
            }
        } catch (RestClientException e) {
            log.error("Error calling random API: {}", e.getMessage(), e);
            return "FAILED";
        }
    }
    //TODO: KRASIVO SDELAT, ENUM
}