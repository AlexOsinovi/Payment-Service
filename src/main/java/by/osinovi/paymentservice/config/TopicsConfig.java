package by.osinovi.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicsConfig {
    private final String ordersTopic;
    private final String paymentsTopic;
    private final String deadOrdersTopic;

    public TopicsConfig(@Value("${spring.kafka.topics.orders}") String ordersTopic,
                       @Value("${spring.kafka.topics.payments}") String paymentsTopic,
                       @Value("${spring.kafka.topics.dead-orders}") String deadOrdersTopic) {
        this.ordersTopic = ordersTopic;
        this.paymentsTopic = paymentsTopic;
        this.deadOrdersTopic = deadOrdersTopic;
    }

    @Bean
    public NewTopic createOrdersTopic() {
        return TopicBuilder.name(ordersTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createPaymentsTopic() {
        return TopicBuilder.name(paymentsTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createDeadOrdersTopic() {
        return TopicBuilder.name(deadOrdersTopic).partitions(1).replicas(1).build();
    }
}