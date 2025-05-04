package com.example.auth.service;

import com.example.auth.entity.UserInfo;
import com.example.auth.repository.UserInfoRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private OtpService otpService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userDetail = repository.findByEmail(username);
        UserInfo user = userDetail.orElseThrow(() -> new UsernameNotFoundException("Account does not exist: " + username));
        if (!user.isVerified()) {
            throw new UsernameNotFoundException("User not verified: " + username);
        }
        return new UserInfoDetails(user);
    }

    @Transactional
    public String addUser(UserInfo userInfo) throws MessagingException, IOException {
        Optional<UserInfo> existingUser = repository.findByEmail(userInfo.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Account already exists: " + userInfo.getEmail());
        }
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        String otp = otpService.generateOtp();
        otpService.saveOtp(userInfo, otp);
        repository.save(userInfo);
        otpService.sendOtpEmail(userInfo.getEmail(), otp);
        return "OTP sent to " + userInfo.getEmail() + ". Please verify to complete registration.";
    }

    @Transactional
    public void verifyUser(String email) {
        Optional<UserInfo> userOptional = repository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Account does not exist: " + email);
        }
        UserInfo user = userOptional.get();
        user.setVerified(true);
        user.setOtpCode(null); // Clear OTP after verification
        user.setOtpExpiresAt(null);
        repository.save(user);
    }

    @Transactional
    public void saveUserWithOtp(String email, String otp) {
        Optional<UserInfo> userOptional = repository.findByEmail(email);
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            otpService.saveOtp(user, otp);
            repository.save(user);
        }
    }

    @Transactional
    public void clearOtp(String email) {
        Optional<UserInfo> userOptional = repository.findByEmail(email);
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            user.setOtpCode(null);
            user.setOtpExpiresAt(null);
            repository.save(user);
        }
    }

    public Optional<UserInfo> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}