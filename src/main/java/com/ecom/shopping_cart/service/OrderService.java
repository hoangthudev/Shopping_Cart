package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.OrderRequest;
import com.ecom.shopping_cart.module.ProductOrder;

public interface OrderService {

    public void saveOrder(Integer userId, OrderRequest orderRequest);
}
