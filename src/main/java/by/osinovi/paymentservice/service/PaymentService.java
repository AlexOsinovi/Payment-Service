package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.dto.OrderMessage;
import by.osinovi.paymentservice.entity.Payment;

public interface PaymentService {
    Payment createPayment(OrderMessage orderMessage);

}
