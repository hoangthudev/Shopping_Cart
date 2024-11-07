package com.ecom.shopping_cart.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;

import java.io.UnsupportedEncodingException;


@Controller
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    public Boolean sendMail(String url, String reciepentEmail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("hoangthu152004@gmail.com", "Shopping Cart");
        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
                + "\">Change my password</a></p>";

        helper.setSubject("Reset your password");
        helper.setText(content, true);

        mailSender.send(message);

        return true;
    }

    public static String generateUrl(HttpServletRequest request) {

        // localhost:8080/forgot-password
        String siteUrl = request.getRequestURL().toString();

        return siteUrl.replace(request.getServletPath(), "");
    }
}
