package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.entity.Payment;

public interface ExternalAPIService {
    String getStatus(Payment payment);
}
