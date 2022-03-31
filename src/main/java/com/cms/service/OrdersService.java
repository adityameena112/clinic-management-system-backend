package com.cms.service;

import com.cms.domain.OrderProducts;
import com.cms.domain.OrderStatus;
import com.cms.domain.Orders;
import com.cms.repository.OrderProductsRepository;
import com.cms.repository.OrderRepository;
import com.cms.repository.UserRepository;
import com.cms.service.dto.OrdersDto;
import com.cms.service.dto.ProductOrderDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class OrdersService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductsRepository orderProductsRepository;

    public OrdersDto makeOrders(OrdersDto ordersDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User u = (User) authentication.getPrincipal();

        com.cms.domain.User user = userRepository.findOneByLogin(u.getUsername()).orElse(null);

        Orders order = new Orders();
        order.setOrderStatus(OrderStatus.BOOKED);
        order.setOrderBy(user);
        order.setOrderDate(LocalDateTime.now());
        Orders o = orderRepository.save(order);

        List<OrderProducts> orderProductsList = new ArrayList<>();
        ordersDto
            .getProducts()
            .forEach(x -> {
                OrderProducts orderProducts = new OrderProducts();
                orderProducts.setOrder(o);
                orderProducts.setQuantity(x.getQuantity());
                orderProducts.setProduct(x.getProduct());
                orderProductsList.add(orderProducts);
            });

        orderProductsRepository.saveAll(orderProductsList);

        return ordersDto;
    }

    public List<OrdersDto> getAllOrdersByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User u = (User) authentication.getPrincipal();
        com.cms.domain.User user = userRepository.findOneByLogin(u.getUsername()).orElse(null);

        List<Orders> orders = orderRepository.getOrdersByUser(user.getId());

        List<OrdersDto> ordersDtos = new ArrayList<>();
        orders
            .stream()
            .forEach(x -> {
                OrdersDto ordersDto = new OrdersDto();
                ordersDto.setId(x.getId());
                ordersDto.setOrderBy(x.getOrderBy());
                ordersDto.setOrderDate(x.getOrderDate());
                ordersDto.setOrderStatus(x.getOrderStatus());
                List<ProductOrderDto> productOrderDtos = x
                    .getOrderProducts()
                    .stream()
                    .map(y -> {
                        ProductOrderDto productOrderDto = new ProductOrderDto();
                        productOrderDto.setProduct(y.getProduct());
                        productOrderDto.setTotalPrice(y.getProduct().getPrice() * y.getQuantity());
                        productOrderDto.setQuantity(y.getQuantity());

                        return productOrderDto;
                    })
                    .collect(Collectors.toList());
                ordersDto.setProducts(productOrderDtos);
                ordersDtos.add(ordersDto);
            });
        return ordersDtos;
    }

    public List<OrdersDto> getAllOrders() {
        List<Orders> orders = orderRepository.getAllOrders();

        List<OrdersDto> ordersDtos = new ArrayList<>();
        orders
            .stream()
            .forEach(x -> {
                OrdersDto ordersDto = new OrdersDto();
                ordersDto.setId(x.getId());
                ordersDto.setOrderBy(x.getOrderBy());
                ordersDto.setOrderDate(x.getOrderDate());
                ordersDto.setOrderStatus(x.getOrderStatus());
                List<ProductOrderDto> productOrderDtos = x
                    .getOrderProducts()
                    .stream()
                    .map(y -> {
                        ProductOrderDto productOrderDto = new ProductOrderDto();
                        productOrderDto.setProduct(y.getProduct());
                        productOrderDto.setTotalPrice(y.getProduct().getPrice() * y.getQuantity());
                        productOrderDto.setQuantity(y.getQuantity());

                        return productOrderDto;
                    })
                    .collect(Collectors.toList());
                ordersDto.setProducts(productOrderDtos);
                ordersDtos.add(ordersDto);
            });
        return ordersDtos;
    }

    public void updateOrderStatus(Long id, OrderStatus orderStatus) {
        Orders order = orderRepository.findById(id).orElse(null);

        if (order != null) {
            order.setOrderStatus(orderStatus);
            orderRepository.save(order);
        }
        //        return order;
    }

    public OrderStatus[] getOrderStatus() {
        return OrderStatus.values();
    }
}