package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;
import com.ecom.shopping_cart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDtls saveUser(UserDtls user) {
        UserDtls saveUser = this.userRepository.save(user);
        return saveUser;
    }
}
