package com.example.auth.service;

import com.example.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.nio.file.Files;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    // Generate a random 6-digit OTP
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(otp);
    }

    // Save OTP to UserInfo
    public void saveOtp(UserInfo userInfo, String otpCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);
        userInfo.setOtpCode(otpCode);
        userInfo.setOtpExpiresAt(expiresAt);
    }

    // Send OTP email with HTML template
    public void sendOtpEmail(String toEmail, String otpCode) throws MessagingException, IOException {
        // Load HTML template
        Resource resource = resourceLoader.getResource("classpath:templates/otp_email_template.html");
        String htmlContent = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        // Replace placeholder with OTP code
        htmlContent = htmlContent.replace("{OTP_CODE}", otpCode);

        // Create and send MIME message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Your OTP for Authentication");
        helper.setText(htmlContent, true); // true indicates HTML content
        mailSender.send(message);
    }

    // Verify OTP
    public boolean verifyOtp(UserInfo userInfo, String otpCode) {
        if (userInfo.getOtpCode() == null || userInfo.getOtpExpiresAt() == null) {
            return false;
        }
        if (userInfo.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (!userInfo.getOtpCode().equals(otpCode)) {
            return false;
        }
        return true;
    }
}