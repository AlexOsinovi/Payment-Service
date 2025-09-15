package by.osinovi.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${random-api-url}")
    private String randomApiUrl;

    @Bean
    public RestClient randomRestClient(RestClient.Builder restClientBuilder) {
        System.out.println("RestClient baseUrl: " + randomApiUrl);
        return restClientBuilder.baseUrl(randomApiUrl).build();
    }
}