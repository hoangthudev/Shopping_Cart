package com.ecom.shopping_cart.service.impl;

import com.ecom.shopping_cart.module.UserDtls;
import com.ecom.shopping_cart.repository.UserRepository;
import com.ecom.shopping_cart.service.UserService;
import com.ecom.shopping_cart.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
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
        user.setAccountNonLock(true);
        user.setFailedAttempt(0);

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

        Optional<UserDtls> findByuser = userRepository.findById(id);

        if (findByuser.isPresent()) {
            UserDtls userDtls = findByuser.get();
            userDtls.setEnable(status);
            userRepository.save(userDtls);
            return true;
        }

        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNonLock(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(UserDtls user) {

        long lockTime = user.getLockTime().getTime();
        long unLockTime = lockTime + AppConstant.UNLOC_DURATION_TIME;

        long currentTime = System.currentTimeMillis();

        if (unLockTime < currentTime) {
            user.setAccountNonLock(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        UserDtls userByEmail = this.userRepository.findByEmail(email);

        userByEmail.setResetToken(resetToken);

        this.userRepository.save(userByEmail);
    }

    @Override
    public UserDtls getUserByToken(String token) {
        return this.userRepository.findByResetToken(token);
    }

    @Override
    public UserDtls updateUser(UserDtls user) {
        return this.userRepository.save(user);
    }

    @Override
    public UserDtls updateUserProfile(UserDtls user, MultipartFile imageProfile) {
        UserDtls dbUser = this.userRepository.findById(user.getId()).get();

        if (!imageProfile.isEmpty()) {
            dbUser.setProfileImage(imageProfile.getOriginalFilename());
        }

        if (!ObjectUtils.isEmpty(dbUser)) {
            dbUser.setName(user.getName());
            dbUser.setMobileNumber(user.getMobileNumber());
            dbUser.setAddress(user.getAddress());
            dbUser.setCity(user.getCity());
            dbUser.setPinCode(user.getPinCode());
            dbUser.setState(user.getState());
            dbUser = this.userRepository.save(dbUser);
        }
        try {
            if (!imageProfile.isEmpty()) {
                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()
                        + File.separator + "profile_img"
                        + File.separator + imageProfile.getOriginalFilename());

//            System.out.println(path);
                Files.copy(imageProfile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbUser;
    }
}
