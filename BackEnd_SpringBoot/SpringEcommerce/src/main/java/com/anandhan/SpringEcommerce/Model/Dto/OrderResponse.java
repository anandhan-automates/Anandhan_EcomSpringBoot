package com.anandhan.SpringEcommerce.Model.Dto;

import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        String orderId,
        String CustomerName,
        String email,
        String Status,
        LocalDate orderDate,
        List<OrderItemResponse> items

) {
}
