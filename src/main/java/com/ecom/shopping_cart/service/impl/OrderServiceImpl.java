package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.Cart;
import com.ecom.shopping_cart.module.OrderAddress;
import com.ecom.shopping_cart.module.OrderRequest;
import com.ecom.shopping_cart.module.ProductOrder;
import com.ecom.shopping_cart.repository.CartRepository;
import com.ecom.shopping_cart.repository.OrderRepository;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.shopping_cart.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;

    @Override
    public void saveOrder(Integer userId, OrderRequest orderRequest) {

        List<Cart> carts = this.cartRepository.getCartByUserId(userId);

        for (Cart cart : carts) {
            ProductOrder order = new ProductOrder();
            order.setOrderId((UUID.randomUUID().toString()));
            order.setOrderDate(new Date());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(orderRequest.getPaymentType());

            OrderAddress address = new OrderAddress();
            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNo(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());

            order.setOrderAddress(address);

            this.orderRepository.save(order);
        }
    }
}
