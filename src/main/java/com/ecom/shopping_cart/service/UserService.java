package com.ecom.shopping_cart.service;

import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    public UserDtls saveUser(UserDtls user);

    public UserDtls getUserByEmail(String email);

    public List<UserDtls> getAllUsers(String role);

    public Boolean updateAccountStatus(Integer id, Boolean status);

    public void increaseFailedAttempt(UserDtls user);

    public void userAccountLock(UserDtls user);

    public Boolean unlockAccountTimeExpired(UserDtls user);

    public void resetAttempt(int userId);

    void updateUserResetToken(String email, String resetToken);

    public UserDtls getUserByToken(String token);

    public UserDtls updateUser(UserDtls user);
}
