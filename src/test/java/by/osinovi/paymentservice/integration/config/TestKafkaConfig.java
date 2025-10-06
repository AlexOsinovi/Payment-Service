package by.osinovi.paymentservice.integration.config;

import by.osinovi.paymentservice.entity.Payment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

import static by.osinovi.paymentservice.integration.KafkaIntegrationTest.kafkaContainer;

@Configuration
public class TestKafkaConfig {


    @Bean(name = "paymentListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Payment> paymentListenerContainerFactory(
            ConsumerFactory<String, Payment> paymentConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Payment> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Payment> paymentConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "by.osinovi.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Payment.class.getName());
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(Payment.class));
    }
}
