package com.pehlione.kafka.dto;

public record CartActionResponse(
        String message,
        int cartItemCount,
        boolean empty,
        Long productId,
        int productQuantity,
        String productLineTotal,
        String cartSubtotal
) {
}
