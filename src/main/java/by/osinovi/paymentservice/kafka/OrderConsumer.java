package by.osinovi.paymentservice.kafka;

import by.osinovi.paymentservice.dto.OrderMessage;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final PaymentService paymentService;
    private final PaymentProducer paymentProducer;

    @KafkaListener(topics = "${spring.kafka.topics.orders}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional(transactionManager = "kafkaTransactionManager")
    public void handleCreateOrder(OrderMessage orderMessage) {
        Payment payment = paymentService.createPayment(orderMessage);
        paymentProducer.sendCreatePaymentEvent(payment);
    }
}