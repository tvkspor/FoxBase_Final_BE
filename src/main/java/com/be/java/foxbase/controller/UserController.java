package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.ResetPasswordRequest;
import com.be.java.foxbase.dto.request.SendOTPRequest;
import com.be.java.foxbase.dto.request.UserCreationRequest;
import com.be.java.foxbase.dto.request.VerifyOTPRequest;
import com.be.java.foxbase.dto.response.*;
import com.be.java.foxbase.service.UserService;
import com.be.java.foxbase.service.RecaptchaService;
import com.be.java.foxbase.utils.RecaptchaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    RecaptchaService recaptchaService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody UserCreationRequest userCreationRequest){
        String token = userCreationRequest.getCaptchaToken();
        Logger log = LoggerFactory.getLogger(UserController.class);

        log.debug("/users/register called - captcha token present={}", token != null && !token.isBlank());

        // Verify captcha
        RecaptchaResponse r = null;
        try {
            r = recaptchaService.verify(token);
        } catch (Exception e) {
            log.error("Error while verifying captcha in /users/register: {}", e.getMessage());
        }

        if (r == null || !r.isSuccess()) {
            log.info("Captcha verification failed for /users/register, errors={}", r == null ? null : r.getErrorCodes());
            return ApiResponse.<UserResponse>builder()
                    .statusCode(400)
                    .message("Captcha verification failed")
                    .build();
        }

        log.info("Captcha verification succeeded for /users/register, hostname={}", r.getHostname());
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(userCreationRequest))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }

    @PostMapping("/request-otp")
    ApiResponse<SendOTPResponse> requestOTP(@RequestBody SendOTPRequest otpRequest){
        return ApiResponse.<SendOTPResponse>builder()
                .data(userService.sendSecurityOTP(otpRequest))
                .build();
    }

    @PostMapping("/verify-otp")
    ApiResponse<VerifyOTPResponse> verifyOTP(@RequestBody VerifyOTPRequest otpRequest){
        return ApiResponse.<VerifyOTPResponse>builder()
                .data(userService.verifySecurityOTP(otpRequest))
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return ApiResponse.<ResetPasswordResponse>builder()
                .data(userService.resetPassword(resetPasswordRequest))
                .build();
    }
}
