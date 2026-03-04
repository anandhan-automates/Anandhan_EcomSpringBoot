package com.anandhan.SpringEcommerce.Model.Dto;

import java.math.BigDecimal;

public record OrderItemRequest(
        int productId,
        int quantity
) {}
