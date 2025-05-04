package com.example.auth.controller;

import com.example.auth.dto.AuthRequest;
import com.example.auth.dto.OtpRequest;
import com.example.auth.entity.UserInfo;
import com.example.auth.service.JwtService;
import com.example.auth.service.OtpService;
import com.example.auth.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInfo userInfo) {
        try {
            return ResponseEntity.ok(userInfoService.addUser(userInfo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest) {
        Optional<UserInfo> userOptional = userInfoService.findByEmail(otpRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account does not exist: " + otpRequest.getEmail());
        }
        UserInfo user = userOptional.get();
        boolean isValid = otpService.verifyOtp(user, otpRequest.getOtpCode());
        if (isValid) {
            userInfoService.verifyUser(otpRequest.getEmail());
            return ResponseEntity.ok("Registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Check if account exists
            Optional<UserInfo> userOptional = userInfoService.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account does not exist: " + request.getEmail());
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String otp = otpService.generateOtp();
                userInfoService.saveUserWithOtp(request.getEmail(), otp);
                otpService.sendOtpEmail(request.getEmail(), otp);
                return ResponseEntity.ok("OTP sent to " + request.getEmail() + ". Please verify to complete login.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<?> verifyLoginOtp(@RequestBody OtpRequest otpRequest) {
        Optional<UserInfo> userOptional = userInfoService.findByEmail(otpRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account does not exist: " + otpRequest.getEmail());
        }
        UserInfo user = userOptional.get();
        boolean isValid = otpService.verifyOtp(user, otpRequest.getOtpCode());
        if (isValid) {
            String token = jwtService.generateToken(otpRequest.getEmail());
            userInfoService.clearOtp(otpRequest.getEmail()); // Clear OTP after verification
            return ResponseEntity.ok("Login successful: " + token);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }

    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }
}