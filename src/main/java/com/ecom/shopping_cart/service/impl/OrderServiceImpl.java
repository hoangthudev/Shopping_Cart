package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.Cart;
import com.ecom.shopping_cart.module.OrderAddress;
import com.ecom.shopping_cart.module.OrderRequest;
import com.ecom.shopping_cart.module.ProductOrder;
import com.ecom.shopping_cart.repository.CartRepository;
import com.ecom.shopping_cart.repository.OrderRepository;
import com.ecom.shopping_cart.service.OrderService;
import com.ecom.shopping_cart.util.CommonUtil;
import com.ecom.shopping_cart.util.OrderStatus;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
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
    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userId, OrderRequest orderRequest) throws MessagingException, UnsupportedEncodingException {

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

            ProductOrder saveOrder = this.orderRepository.save(order);
            this.commonUtil.sendEmailForProductOrder(saveOrder, "Success");
        }
    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = this.orderRepository.getByUserId(userId);
        return orders;
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = this.orderRepository.findById(id);
        if (findById.isPresent()) {
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);
            ProductOrder updateOrder = this.orderRepository.save(productOrder);
            return updateOrder;
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrder() {
        List<ProductOrder> orders = this.orderRepository.findAll();
        return orders;
    }

    @Override
    public ProductOrder getOrderById(String orderId) {
        return this.orderRepository.findByOrderId(orderId);
    }

    @Override
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return this.orderRepository.findAll(pageable);
    }
}
