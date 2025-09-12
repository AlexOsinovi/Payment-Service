package by.osinovi.paymentservice.service;

import by.osinovi.paymentservice.util.PaymentStatus;

public interface ExternalAPIService {
    PaymentStatus getStatus();
}
