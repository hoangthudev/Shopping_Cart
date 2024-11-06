package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;
import com.ecom.shopping_cart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setRole("ROLE_USER");
        user.setEnable(true);
        String encodePassword = this.passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = this.userRepository.save(user);
        return saveUser;
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public List<UserDtls> getAllUsers(String role) {
        List<UserDtls> allUserWithRole = this.userRepository.findByRole(role);
        return allUserWithRole;
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {

        Optional<UserDtls> findByUser = this.userRepository.findById(id);

        if (findByUser.isPresent()) {
            UserDtls userDtls = findByUser.get();
            userDtls.setEnable(status);
            this.userRepository.save(userDtls);
            return true;
        }

        return false;
    }
}
