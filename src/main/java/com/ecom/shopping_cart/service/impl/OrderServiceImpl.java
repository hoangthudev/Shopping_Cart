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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
            order.setOrderDate(LocalDate.now());
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

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = this.orderRepository.getByUserId(userId);
        return orders;
    }

    @Override
    public Boolean updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = this.orderRepository.findById(id);
        if (findById.isPresent()) {
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);
            this.orderRepository.save(productOrder);
            return true;
        }
        return false;
    }

    @Override
    public List<ProductOrder> getAllOrder() {
        List<ProductOrder> orders = this.orderRepository.findAll();
        return orders;
    }
}
