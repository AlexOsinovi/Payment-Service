package by.osinovi.paymentservice.kafka;

import by.osinovi.paymentservice.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {
    private final KafkaTemplate<String, Payment> paymentKafkaTemplate;

    @Value("${spring.kafka.topics.payments}")
    private String paymentsTopic;

    public void sendCreatePaymentEvent(Payment payment) {
        log.warn("Sending payment event to topic {}: {}", paymentsTopic, payment);
        paymentKafkaTemplate.send(paymentsTopic, String.valueOf(payment.getId()), payment);
        log.warn("Payment event sent successfully");
    }
}