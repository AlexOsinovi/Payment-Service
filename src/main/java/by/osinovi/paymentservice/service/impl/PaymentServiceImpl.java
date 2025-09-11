package by.osinovi.paymentservice.service.impl;

import by.osinovi.paymentservice.dto.payment.PaymentRequestDTO;
import by.osinovi.paymentservice.dto.payment.PaymentResponseDTO;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.kafka.PaymentProducer;
import by.osinovi.paymentservice.mapper.PaymentMapper;
import by.osinovi.paymentservice.repository.PaymentRepository;
import by.osinovi.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ExternalAPIServiceImpl randomService;
    private final PaymentProducer paymentProducer;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setId(UUID.randomUUID());
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus("CREATED");
        Payment saved = paymentRepository.save(payment);
        paymentProducer.sendCreatePaymentEvent(paymentMapper.toMessage(saved));

        String finalStatus = randomService.getStatus(payment);
        saved.setStatus(finalStatus);
        paymentRepository.save(saved);
        paymentProducer.sendCreatePaymentEvent(paymentMapper.toMessage(saved));

        return paymentMapper.toResponseDto(saved);
    }


    public List<PaymentResponseDTO> findPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(Long.valueOf(orderId)).stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }

    public List<PaymentResponseDTO> findPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(Long.valueOf(userId)).stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }


    public List<PaymentResponseDTO> findPaymentsByStatus(List<String> statuses) {
        return paymentRepository.findByStatusIn(statuses).stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }


    public Double getTotalAmountByDateRange(String start, String end) {
        return paymentRepository.sumPaymentAmountByDateRange(LocalDateTime.parse(start), LocalDateTime.parse(end))
                .orElse(0.0);
    }


    public void deletePayment(String id) {
        if (!paymentRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Payment with ID " + id + " not found");
        }
        paymentRepository.deleteById(UUID.fromString(id));
    }

}