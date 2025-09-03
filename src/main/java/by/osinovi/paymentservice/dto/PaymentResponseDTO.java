package by.osinovi.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private String id;
    private Long orderId;
    private Long userId;
    private String status;
    private LocalDateTime timestamp;
    private Double paymentAmount;
}