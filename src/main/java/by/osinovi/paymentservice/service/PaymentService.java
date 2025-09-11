package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.dto.payment.PaymentRequestDTO;
import by.osinovi.paymentservice.dto.payment.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO dto);

    List<PaymentResponseDTO> findPaymentsByOrderId(String orderId);

    List<PaymentResponseDTO> findPaymentsByUserId(String userId);

    List<PaymentResponseDTO> findPaymentsByStatus(List<String> statuses);

    Double getTotalAmountByDateRange(String start, String end);

    void deletePayment(String id);
}
