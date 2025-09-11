package by.osinovi.paymentservice.kafka;

import by.osinovi.paymentservice.dto.message.PaymentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentProducer {
    private final KafkaTemplate<String, PaymentMessage> paymentKafkaTemplate;
    private final String paymentsTopic;

    public PaymentProducer(KafkaTemplate<String, PaymentMessage> paymentKafkaTemplate,
                           @Value("${spring.kafka.topics.payments}") String paymentsTopic) {
        this.paymentKafkaTemplate = paymentKafkaTemplate;
        this.paymentsTopic = paymentsTopic;
    }

    public void sendCreatePaymentEvent(PaymentMessage paymentMessage) {
        paymentKafkaTemplate.send(paymentsTopic, String.valueOf(paymentMessage.getPaymentId()), paymentMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send CREATE_PAYMENT event for paymentId: {}", paymentMessage.getPaymentId(), ex);
                        throw new RuntimeException("Failed to send CREATE_PAYMENT event", ex);
                    }
                    log.info("CREATE_PAYMENT event sent for paymentId: {}", paymentMessage.getPaymentId());
                });
    }
}