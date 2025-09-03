package by.osinovi.paymentservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    private static final String randomApiUrl = "https://www.random.org";

    @Bean
    public RestClient randomRestClient(RestClient.Builder restClientBuilder) {
        logger.info("Configuring RestClient with baseUrl: {}", randomApiUrl);
        return restClientBuilder.baseUrl(randomApiUrl).build();
    }
}