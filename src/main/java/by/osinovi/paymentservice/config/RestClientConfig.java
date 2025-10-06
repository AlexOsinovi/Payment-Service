package by.osinovi.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private static final String randomApiUrl = "https://www.random.org";

    @Bean
    public RestClient randomRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(randomApiUrl).build();
    }
}