package by.osinovi.paymentservice.kafka;

import by.osinovi.paymentservice.dto.message.OrderMessage;
import by.osinovi.paymentservice.dto.payment.PaymentRequestDTO;
import by.osinovi.paymentservice.mapper.PaymentMapper;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @KafkaListener(topics = "${spring.kafka.topics.orders}", groupId = "payment-group",
            containerFactory = "orderListenerContainerFactory")
    public void handleCreateOrder(OrderMessage orderMessage) {
        try {
            log.info("Received CREATE_ORDER event: {}", orderMessage);
            Long orderId = orderMessage.getOrderId();
            log.debug("Checking if payment exists for orderId: {}", orderId);
            if (paymentRepository.existsByOrderId(orderId)) {
                log.info("Order {} already processed, skipping.", orderId);
                return;
            }

            log.debug("Mapping OrderMessage to PaymentRequestDTO");
            PaymentRequestDTO paymentRequest = paymentMapper.toRequest(orderMessage);
            log.debug("Mapped PaymentRequestDTO: {}", paymentRequest);
            paymentService.createPayment(paymentRequest);
            log.info("Payment created for orderId: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process CREATE_ORDER for orderId: {}. Message: {}",
                    orderMessage.getOrderId(), orderMessage, e);
            throw new RuntimeException("Failed to handle CREATE_ORDER", e);
        }
    }
}