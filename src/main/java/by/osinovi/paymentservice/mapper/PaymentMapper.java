package by.osinovi.paymentservice.mapper;

import by.osinovi.paymentservice.dto.PaymentRequestDTO;
import by.osinovi.paymentservice.dto.PaymentResponseDTO;
import by.osinovi.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "payment_amount", source = "paymentAmount")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Payment toEntity(PaymentRequestDTO dto);

    @Mapping(target = "paymentAmount", source = "payment_amount")
    PaymentResponseDTO toResponseDto(Payment entity);
}