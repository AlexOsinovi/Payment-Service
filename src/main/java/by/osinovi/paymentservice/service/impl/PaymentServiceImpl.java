package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.dto.OrderMessage;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.service.ExternalAPIService;
import by.osinovi.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ExternalAPIService externalAPIService;

    @Override
    @Transactional
    public Payment createPayment(OrderMessage orderMessage) {
        if (orderMessage == null) {
            throw new IllegalArgumentException("OrderMessage cannot be null");
        }
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderMessage.getOrderId());
        payment.setUserId(orderMessage.getUserId());
        payment.setPayment_amount(orderMessage.getTotalAmount());
        payment.setTimestamp(LocalDateTime.now());

        payment.setStatus(externalAPIService.getStatus());

        return paymentRepository.save(payment);
    }

    public Double getTotalAmountByDateRange(String start, String end) {
        return paymentRepository.sumPaymentAmountByDateRange(LocalDateTime.parse(start), LocalDateTime.parse(end))
                .orElse(0.0);
    }

}