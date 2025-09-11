package by.osinovi.paymentservice.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private UUID id;
    private Long orderId;
    private Long userId;
    private String status;
    private LocalDateTime timestamp;
    private Double paymentAmount;
}