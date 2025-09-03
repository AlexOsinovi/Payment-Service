package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.dto.PaymentRequestDTO;
import by.osinovi.paymentservice.dto.PaymentResponseDTO;
import by.osinovi.paymentservice.entity.Payment;
import by.osinovi.paymentservice.mapper.PaymentMapper;
import by.osinovi.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomService randomService;

    public PaymentResponseDTO createPayment(PaymentRequestDTO dto) {
        Payment payment = paymentMapper.toEntity(dto);
        payment.setId(UUID.randomUUID());
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus(randomService.getRandomStatus(payment));
        Payment saved = paymentRepository.save(payment);
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