package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.dto.OrderMessage;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.service.ExternalAPIService;
import by.osinovi.paymentservice.util.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ExternalAPIService externalAPIService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private OrderMessage orderMessage;
    private Payment expectedPayment;

    @BeforeEach
    void setUp() {
        orderMessage = new OrderMessage();
        orderMessage.setOrderId(123L);
        orderMessage.setUserId(456L);
        orderMessage.setTotalAmount(new BigDecimal("100.50"));

        expectedPayment = new Payment();
        expectedPayment.setId(UUID.randomUUID());
        expectedPayment.setOrderId(123L);
        expectedPayment.setUserId(456L);
        expectedPayment.setTimestamp(LocalDateTime.now());
        expectedPayment.setStatus(PaymentStatus.SUCCESS);
        expectedPayment.setPayment_amount(new BigDecimal("100.50"));
    }

    @Test
    void createPayment_ShouldCreatePaymentSuccessfully_WhenValidOrderMessage() {
        when(externalAPIService.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result);
        assertEquals(expectedPayment.getOrderId(), result.getOrderId());
        assertEquals(expectedPayment.getUserId(), result.getUserId());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getId());
        assertNotNull(result.getTimestamp());

        verify(externalAPIService, times(1)).getStatus();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_ShouldCreatePaymentWithFailedStatus_WhenExternalAPIReturnsFailed() {
        when(externalAPIService.getStatus()).thenReturn(PaymentStatus.FAILED);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });

        Payment result = paymentService.createPayment(orderMessage);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, result.getStatus());
        verify(externalAPIService, times(1)).getStatus();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_ShouldSetCorrectTimestamp_WhenCreatingPayment() {
        LocalDateTime beforeTest = LocalDateTime.now();
        when(externalAPIService.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });

        Payment result = paymentService.createPayment(orderMessage);

        LocalDateTime afterTest = LocalDateTime.now();
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isAfter(beforeTest.minusSeconds(1)));
        assertTrue(result.getTimestamp().isBefore(afterTest.plusSeconds(1)));
    }

    @Test
    void createPayment_ShouldGenerateUniqueId_WhenCreatingPayment() {
        when(externalAPIService.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });

        Payment result1 = paymentService.createPayment(orderMessage);
        Payment result2 = paymentService.createPayment(orderMessage);

        assertNotNull(result1.getId());
        assertNotNull(result2.getId());
        assertNotEquals(result1.getId(), result2.getId());
    }

    @Test
    void getTotalAmountByDateRange_ShouldReturnCorrectSum_WhenValidDateRange() {
        String startDate = "2024-01-01T00:00:00";
        String endDate = "2024-01-31T23:59:59";
        Double expectedSum = 1500.75;
        when(paymentRepository.sumPaymentAmountByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(java.util.Optional.of(expectedSum));

        Double result = paymentService.getTotalAmountByDateRange(startDate, endDate);

        assertEquals(expectedSum, result);
        verify(paymentRepository, times(1)).sumPaymentAmountByDateRange(
                LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Test
    void getTotalAmountByDateRange_ShouldReturnZero_WhenNoPaymentsFound() {
        String startDate = "2024-01-01T00:00:00";
        String endDate = "2024-01-31T23:59:59";
        when(paymentRepository.sumPaymentAmountByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(java.util.Optional.empty());

        Double result = paymentService.getTotalAmountByDateRange(startDate, endDate);

        assertEquals(0.0, result);
        verify(paymentRepository, times(1)).sumPaymentAmountByDateRange(
                LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    @Test
    void getTotalAmountByDateRange_ShouldThrowException_WhenInvalidDateFormat() {
        String invalidStartDate = "invalid-date";
        String endDate = "2024-01-31T23:59:59";

        assertThrows(Exception.class, () ->
            paymentService.getTotalAmountByDateRange(invalidStartDate, endDate));
    }
}
