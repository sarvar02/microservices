package com.example.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private long orderId;
    private Instant orderDate;
    private String orderStatus;
    private long amount;
    private ProductDetails productDetails;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDetails {
        private Long productId;
        private String productName;
        private Long price;
        private Long quantity;
    }

}
