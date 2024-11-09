package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.OrderRequest;
import com.ecom.shopping_cart.module.ProductOrder;

import java.util.List;

public interface OrderService {

    public void saveOrder(Integer userId, OrderRequest orderRequest);

    public List<ProductOrder> getOrdersByUser(Integer userId);

    public Boolean updateOrderStatus(Integer id, String status);
}
