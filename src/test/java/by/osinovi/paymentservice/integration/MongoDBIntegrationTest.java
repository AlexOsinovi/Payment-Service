package by.osinovi.paymentservice.integration;

import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.integration.config.BaseIntegrationTest;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.util.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Testcontainers
class MongoDBIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        
        testPayment = new Payment();
        testPayment.setId(UUID.randomUUID());
        testPayment.setOrderId(123L);
        testPayment.setUserId(456L);
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setTimestamp(LocalDateTime.now());
        testPayment.setPayment_amount(new BigDecimal("100.50"));
    }

    @Test
    void savePayment_ShouldPersistPaymentInMongoDB() {
        Payment savedPayment = paymentRepository.save(testPayment);

        assertNotNull(savedPayment);
        assertEquals(testPayment.getOrderId(), savedPayment.getOrderId());
        assertEquals(testPayment.getUserId(), savedPayment.getUserId());
        assertEquals(testPayment.getStatus(), savedPayment.getStatus());
        assertEquals(testPayment.getPayment_amount(), savedPayment.getPayment_amount());
        assertEquals(testPayment.getTimestamp(), savedPayment.getTimestamp());
        
        Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getId());
        assertTrue(foundPayment.isPresent());
        assertEquals(savedPayment.getId(), foundPayment.get().getId());
    }

    @Test
    void findByUserId_ShouldReturnCorrectPayments() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        Payment payment3 = createTestPayment(125L, 789L, PaymentStatus.SUCCESS);
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> userPayments = paymentRepository.findByUserId(456L);

        assertEquals(2, userPayments.size());
        assertTrue(userPayments.stream().allMatch(p -> p.getUserId().equals(456L)));
    }

    @Test
    void findByOrderId_ShouldReturnCorrectPayment() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        
        paymentRepository.saveAll(List.of(payment1, payment2));

        List<Payment> orderPayments = paymentRepository.findByOrderId(123L);

        assertEquals(1, orderPayments.size());
        assertEquals(123L, orderPayments.get(0).getOrderId());
    }

    @Test
    void findByStatusIn_ShouldReturnPaymentsWithMatchingStatuses() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        Payment payment3 = createTestPayment(125L, 789L, PaymentStatus.SUCCESS);
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> successPayments = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS));

        assertEquals(2, successPayments.size());
        assertTrue(successPayments.stream().allMatch(p -> p.getStatus() == PaymentStatus.SUCCESS));
    }

    @Test
    void sumPaymentAmountByDateRange_ShouldCalculateCorrectSum() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 12, 0, 0);
        
        Payment payment1 = createTestPaymentWithTime(123L, 456L, PaymentStatus.SUCCESS, 
                baseTime, new BigDecimal("100.00"));
        Payment payment2 = createTestPaymentWithTime(124L, 456L, PaymentStatus.SUCCESS, 
                baseTime.plusDays(5), new BigDecimal("200.50"));
        Payment payment3 = createTestPaymentWithTime(125L, 789L, PaymentStatus.SUCCESS, 
                baseTime.plusDays(20), new BigDecimal("150.25")); // Outside range
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59, 59);

        Optional<Double> sum = paymentRepository.sumPaymentAmountByDateRange(startDate, endDate);

        assertTrue(sum.isPresent());
        assertEquals(300.50, sum.get(), 0.01);
    }

    @Test
    void sumPaymentAmountByDateRange_ShouldReturnEmpty_WhenNoPaymentsInRange() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 12, 0, 0);
        Payment payment = createTestPaymentWithTime(123L, 456L, PaymentStatus.SUCCESS, 
                baseTime, new BigDecimal("100.00"));
        paymentRepository.save(payment);

        LocalDateTime startDate = LocalDateTime.of(2024, 2, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 2, 28, 23, 59, 59);

        Optional<Double> sum = paymentRepository.sumPaymentAmountByDateRange(startDate, endDate);

        assertFalse(sum.isPresent());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenPaymentExists() {
        Payment savedPayment = paymentRepository.save(testPayment);

        boolean exists = paymentRepository.existsById(savedPayment.getId());

        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenPaymentDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        boolean exists = paymentRepository.existsById(nonExistentId);

        assertFalse(exists);
    }

    @Test
    void deleteById_ShouldRemovePaymentFromDatabase() {
        Payment savedPayment = paymentRepository.save(testPayment);
        assertTrue(paymentRepository.existsById(savedPayment.getId()));

        paymentRepository.deleteById(savedPayment.getId());

        assertFalse(paymentRepository.existsById(savedPayment.getId()));
    }

    @Test
    void saveAll_ShouldPersistMultiplePayments() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        Payment payment3 = createTestPayment(125L, 789L, PaymentStatus.SUCCESS);
        List<Payment> payments = List.of(payment1, payment2, payment3);

        List<Payment> savedPayments = paymentRepository.saveAll(payments);

        assertEquals(3, savedPayments.size());
        assertEquals(3, paymentRepository.count());
        assertTrue(paymentRepository.existsById(payment1.getId()));
        assertTrue(paymentRepository.existsById(payment2.getId()));
        assertTrue(paymentRepository.existsById(payment3.getId()));
    }

    @Test
    void findByStatusIn_WithMultipleStatuses_ShouldReturnCorrectPayments() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        Payment payment3 = createTestPayment(125L, 789L, PaymentStatus.SUCCESS);
        
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> successPayments = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS));

        assertEquals(2, successPayments.size());
        assertTrue(successPayments.stream().allMatch(p -> p.getStatus() == PaymentStatus.SUCCESS));
    }

    @Test
    void count_ShouldReturnCorrectNumberOfPayments() {
        assertEquals(0, paymentRepository.count());
        
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        
        paymentRepository.save(payment1);
        assertEquals(1, paymentRepository.count());
        
        paymentRepository.save(payment2);
        assertEquals(2, paymentRepository.count());
    }

    @Test
    void findAll_ShouldReturnAllPayments() {
        Payment payment1 = createTestPayment(123L, 456L, PaymentStatus.SUCCESS);
        Payment payment2 = createTestPayment(124L, 456L, PaymentStatus.FAILED);
        paymentRepository.saveAll(List.of(payment1, payment2));

        List<Payment> allPayments = paymentRepository.findAll();

        assertEquals(2, allPayments.size());
        assertTrue(allPayments.stream().anyMatch(p -> p.getId().equals(payment1.getId())));
        assertTrue(allPayments.stream().anyMatch(p -> p.getId().equals(payment2.getId())));
    }

    private Payment createTestPayment(Long orderId, Long userId, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(status);
        payment.setTimestamp(LocalDateTime.now());
        payment.setPayment_amount(new BigDecimal("100.00"));
        return payment;
    }

    private Payment createTestPaymentWithTime(Long orderId, Long userId, PaymentStatus status, 
                                            LocalDateTime timestamp, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setStatus(status);
        payment.setTimestamp(timestamp);
        payment.setPayment_amount(amount);
        return payment;
    }
}

