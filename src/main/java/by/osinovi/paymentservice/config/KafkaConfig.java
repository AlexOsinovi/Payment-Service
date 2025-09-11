package by.osinovi.paymentservice.config;

import by.osinovi.paymentservice.dto.message.OrderMessage;
import by.osinovi.paymentservice.dto.message.PaymentMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    private final String kafkaUri;

    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String kafkaUri) {
        this.kafkaUri = kafkaUri;
    }

    @Bean
    public ConsumerFactory<String, OrderMessage> orderConsumerFactory() {
        JsonDeserializer<OrderMessage> deserializer = new JsonDeserializer<>(OrderMessage.class, false);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderMessage> orderListenerContainerFactory(
            KafkaTemplate<String, OrderMessage> dlqKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, OrderMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConsumerFactory());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate, (message, exception) -> new TopicPartition("dead_orders", message.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3)));
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public ProducerFactory<String, PaymentMessage> paymentProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, PaymentMessage> paymentKafkaTemplate() {
        return new KafkaTemplate<>(paymentProducerFactory());
    }

    @Bean
    public ProducerFactory<String, OrderMessage> dlqProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, OrderMessage> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    private Map<String, Object> defaultProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }
}