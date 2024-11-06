package com.ecom.shopping_cart.repository;

import com.ecom.shopping_cart.module.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDtls, Integer> {
}
