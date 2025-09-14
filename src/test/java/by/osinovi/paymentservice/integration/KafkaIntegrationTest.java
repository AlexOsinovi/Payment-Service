package by.osinovi.paymentservice.integration;

import by.osinovi.paymentservice.dto.OrderMessage;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.kafka.PaymentProducer;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.service.PaymentService;
import by.osinovi.paymentservice.util.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@DirtiesContext
public class KafkaIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withReuse(true)
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withReuse(true)
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.enable-auto-commit", () -> "false");
        registry.add("spring.kafka.consumer.group-id", () -> "payment-group");
        registry.add("spring.kafka.topics.orders", () -> "orders-topic");
        registry.add("spring.kafka.topics.payments", () -> "payments-topic");
        registry.add("spring.kafka.producer.properties.spring.json.add.type.headers", () -> false);
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "by.osinovi.*");

        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;


    private final BlockingQueue<ConsumerRecord<String, Payment>> paymentEvents = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        paymentEvents.clear();
    }

    @Test
    void fullKafkaFlow_ShouldProcessOrderMessageAndSendPaymentEvent() throws Exception {
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(123L);
        orderMessage.setUserId(456L);
        orderMessage.setTotalAmount(new BigDecimal("100.50"));

        paymentEvents.clear();

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send("orders-topic", String.valueOf(orderMessage.getOrderId()), orderMessage);
            return null;
        });

        Thread.sleep(20000);

        List<Payment> payments = paymentRepository.findByOrderId(orderMessage.getOrderId());
        assertFalse(payments.isEmpty(), "No payment found for orderId: " + orderMessage.getOrderId());

        Payment createdPayment = payments.getFirst();
        assertEquals(orderMessage.getOrderId(), createdPayment.getOrderId());
        assertEquals(orderMessage.getUserId(), createdPayment.getUserId());
        assertEquals(orderMessage.getTotalAmount(), createdPayment.getPayment_amount());
        assertNotNull(createdPayment.getStatus());
        assertNotNull(createdPayment.getTimestamp());

        ConsumerRecord<String, Payment> receivedEvent = paymentEvents.poll(10, TimeUnit.SECONDS);
        assertNotNull(receivedEvent, "No payment event was received within 10 seconds");
        assertEquals(createdPayment.getId().toString(), receivedEvent.key());
        assertEquals(createdPayment.getId(), receivedEvent.value().getId());
        assertEquals(createdPayment.getOrderId(), receivedEvent.value().getOrderId());
        assertEquals(createdPayment.getUserId(), receivedEvent.value().getUserId());
        assertEquals(createdPayment.getPayment_amount(), receivedEvent.value().getPayment_amount());
    }

    @Test
    void handleCreateOrder_ShouldProcessOrderMessageAndCreatePayment() throws Exception {
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(123L);
        orderMessage.setUserId(456L);
        orderMessage.setTotalAmount(new BigDecimal("100.50"));

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result);
        assertEquals(orderMessage.getOrderId(), result.getOrderId());
        assertEquals(orderMessage.getUserId(), result.getUserId());
        assertNotNull(result.getId());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getStatus());

        assertTrue(paymentRepository.existsById(result.getId()));
    }

    @Test
    void paymentProducer_ShouldSerializePaymentCorrectly() throws Exception {
        Payment payment = createTestPayment();

        String paymentJson = objectMapper.writeValueAsString(payment);
        Payment deserializedPayment = objectMapper.readValue(paymentJson, Payment.class);

        assertEquals(payment.getId(), deserializedPayment.getId());
        assertEquals(payment.getOrderId(), deserializedPayment.getOrderId());
        assertEquals(payment.getUserId(), deserializedPayment.getUserId());
        assertEquals(payment.getStatus(), deserializedPayment.getStatus());
        assertEquals(payment.getPayment_amount(), deserializedPayment.getPayment_amount());
        assertEquals(payment.getTimestamp(), deserializedPayment.getTimestamp());
    }


    @Test
    @Transactional(transactionManager = "kafkaTransactionManager")
    void multiplePayments_ShouldBeProcessedCorrectly() throws Exception {
        OrderMessage orderMessage1 = createOrderMessage(123L, 456L, new BigDecimal("100.00"));
        OrderMessage orderMessage2 = createOrderMessage(124L, 456L, new BigDecimal("200.00"));
        OrderMessage orderMessage3 = createOrderMessage(125L, 789L, new BigDecimal("150.00"));

        Payment payment1 = paymentService.createPayment(orderMessage1);
        Payment payment2 = paymentService.createPayment(orderMessage2);
        Payment payment3 = paymentService.createPayment(orderMessage3);

        assertEquals(3, paymentRepository.count());
        assertTrue(paymentRepository.existsById(payment1.getId()));
        assertTrue(paymentRepository.existsById(payment2.getId()));
        assertTrue(paymentRepository.existsById(payment3.getId()));

        assertNotEquals(payment1.getId(), payment2.getId());
        assertNotEquals(payment2.getId(), payment3.getId());
        assertNotEquals(payment1.getId(), payment3.getId());
    }



    @Test
    void paymentStatus_ShouldBeSetCorrectly() throws Exception {
        OrderMessage orderMessage = createOrderMessage(123L, 456L, new BigDecimal("100.00"));

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result.getStatus());
        assertTrue(result.getStatus() == PaymentStatus.SUCCESS || result.getStatus() == PaymentStatus.FAILED);
        assertEquals(orderMessage.getTotalAmount(), result.getPayment_amount());
    }

    @Test
    void createPayment_WithNullOrderMessage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(null));
    }

    @Test
    void simpleKafkaTest_ShouldWork() throws Exception {
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(999L);
        orderMessage.setUserId(888L);
        orderMessage.setTotalAmount(new BigDecimal("50.00"));

        Payment payment = paymentService.createPayment(orderMessage);

        assertNotNull(payment);
        assertEquals(orderMessage.getOrderId(), payment.getOrderId());
        assertEquals(orderMessage.getUserId(), payment.getUserId());
        assertEquals(orderMessage.getTotalAmount(), payment.getPayment_amount());
        assertNotNull(payment.getStatus());
        assertNotNull(payment.getTimestamp());

        assertTrue(paymentRepository.existsById(payment.getId()));
    }

    @Test
    void createPayment_WithZeroAmount_ShouldProcessCorrectly() throws Exception {
        OrderMessage orderMessage = createOrderMessage(123L, 456L, BigDecimal.ZERO);

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPayment_amount());
        assertTrue(paymentRepository.existsById(result.getId()));
    }

    @Test
    void createPayment_WithLargeAmount_ShouldProcessCorrectly() throws Exception {
        OrderMessage orderMessage = createOrderMessage(123L, 456L, new BigDecimal("999999.99"));

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result);
        assertEquals(new BigDecimal("999999.99"), result.getPayment_amount());
        assertTrue(paymentRepository.existsById(result.getId()));
    }


    @KafkaListener(topics = "payments-topic", groupId = "payment-test-group",containerFactory = "paymentListenerContainerFactory")
    public void listenToPaymentEvents(ConsumerRecord<String, Payment> record) {
        paymentEvents.add(record);
    }

    private Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(123L);
        payment.setUserId(456L);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(java.time.LocalDateTime.now());
        payment.setPayment_amount(new BigDecimal("100.50"));
        return payment;
    }

    private OrderMessage createOrderMessage(Long orderId, Long userId, BigDecimal totalAmount) {
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOrderId(orderId);
        orderMessage.setUserId(userId);
        orderMessage.setTotalAmount(totalAmount);
        return orderMessage;
    }
}