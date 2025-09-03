package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.entity.Payment;

public interface RandomService {
    String getRandomStatus(Payment payment);
}
