package com.lsb.webshop.service;

import com.lsb.webshop.domain.Order;
import com.lsb.webshop.domain.OrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.lsb.webshop.repository.OrderDetailRepository;
import com.lsb.webshop.repository.OrderRepository;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public List<Order> fetchAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> fetchOrderById(long id) {
        return orderRepository.findById(id);
    }

    public void deleteOderById(long id) {
        Optional<Order> orderOptional = this.fetchOrderById(id);
        log.info("delete order id {}", id);
        if (orderOptional.isPresent()){
            Order order = orderOptional.get();
            List<OrderDetail> orderDetails = order.getOrderDetail();

            for (OrderDetail orderDetail : orderDetails) {
                this.orderRepository.deleteById(orderDetail.getId());
            }
        }
        this.orderRepository.deleteById(id);

    }

}
