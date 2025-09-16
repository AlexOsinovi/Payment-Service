package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.service.ExternalAPIService;
import by.osinovi.paymentservice.util.PaymentStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalAPIServiceImpl implements ExternalAPIService {

    @Value("${random-api-url}")
    private String uri;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentStatus getStatus() {
        try {
            String body = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (body != null && !body.isBlank()) {
                try {
                    List<Integer> numbers = objectMapper.readValue(body, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Integer.class));
                    if (numbers.isEmpty()) {
                        throw new RuntimeException("Empty response array from API");
                    }
                    int randomNumber = numbers.get(0);
                    log.info("Received random number: {}", randomNumber);
                    return randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse JSON response: {}", e.getMessage(), e);
                    throw new RuntimeException("Invalid JSON response from API", e);
                }
            } else {
                throw new RuntimeException("Empty or invalid response from API");
            }
        } catch (RestClientException e) {
            log.error("Error calling random API: {}", e.getMessage(), e);
            return PaymentStatus.FAILED;
        }
    }
}