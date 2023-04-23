package com.example.orderservice.external.client;

import com.example.orderservice.model.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);
}
