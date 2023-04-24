package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.CustomException;
import com.example.orderservice.external.client.PaymentService;
import com.example.orderservice.external.client.ProductService;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderResponse;
import com.example.orderservice.model.PaymentRequest;
import com.example.orderservice.repository.OrderRepository;
import com.example.productservice.model.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Long placeOrder(OrderRequest orderRequest) {

        log.info("Placing order request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                        .orderId(order.getId())
                        .paymentMode(orderRequest.getPaymentMode())
                        .amount(orderRequest.getTotalAmount())
                        .build();

        String orderStatus = null;

        try{
            paymentService.doPayment(paymentRequest);

            log.info("Payment done Successfully. Changing the Order status to PLACED");
            orderStatus = "PLACED";

        }catch (Exception e){
            log.error("Error occurred in payment. Changing the Order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Places successfully with Order Id: {}", order.getId());

        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {

        log.info("Get order details for Order id: {}", orderId);

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id: " + orderId, "NOT_FOUND", 404));

        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());

        ProductResponse productResponse =
                restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails
                .builder()
                .productId(productResponse.getProductId())
                .productName(productResponse.getProductName())
                .price(productResponse.getPrice())
                .quantity(productResponse.getQuantity())
                .build();

        OrderResponse orderResponse = OrderResponse
                .builder()
                .orderId(orderId)
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .build();

        return orderResponse;
    }
}
