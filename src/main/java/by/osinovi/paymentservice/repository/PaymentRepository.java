package by.osinovi.paymentservice.repository;

import by.osinovi.paymentservice.entity.Payment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends MongoRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatusIn(List<String> statuses);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { _id: null, total: { $sum: '$payment_amount' } } }"
    })
    Optional<Double> sumPaymentAmountByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    boolean existsByOrderId(Long orderId);
}
