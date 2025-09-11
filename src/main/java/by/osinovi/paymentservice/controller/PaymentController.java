package by.osinovi.paymentservice.controller;

import by.osinovi.paymentservice.dto.payment.PaymentRequestDTO;
import by.osinovi.paymentservice.dto.payment.PaymentResponseDTO;
import by.osinovi.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.ok().body(paymentService.createPayment(dto));
    }

    @GetMapping("/order_id/{id}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentByOrderId(@PathVariable String id) {
        return ResponseEntity.ok().body(paymentService.findPaymentsByOrderId(id));
    }

    @GetMapping("/user_id/{id}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentByUserId(@PathVariable String id) {
        return ResponseEntity.ok().body(paymentService.findPaymentsByUserId(id));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStatus(@RequestParam List<String> statuses) {
        return ResponseEntity.ok().body(paymentService.findPaymentsByStatus(statuses));

    }

    @GetMapping("/total_amount")
    public ResponseEntity<Double> getTotalAmountByDateRange(@RequestParam("start") String start,
                                                            @RequestParam("end") String end) {
        return ResponseEntity.ok().body(paymentService.getTotalAmountByDateRange(start, end));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}