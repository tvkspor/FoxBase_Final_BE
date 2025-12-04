package com.be.java.foxbase.controller;

import com.be.java.foxbase.service.RecaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RecaptchaService recaptchaService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String token = body.get("captchaToken") == null ? null : body.get("captchaToken").toString();

        // Log presence of token
        log.debug("/api/auth/register called - captcha token present={}", token != null && !token.isBlank());

        // Call verify to get detailed response (error-codes, hostname) for debugging
        var resp = recaptchaService.verify(token);
        if (resp == null || !resp.isSuccess()) {
            // Log error codes
            log.info("Captcha verification failed for /api/auth/register, errors={}", resp == null ? null : resp.getErrorCodes());
            // Return safe details why verification failed
            return ResponseEntity.badRequest().body(
                Map.of(
                    "message", "Captcha verification failed",
                    "errors", resp == null ? null : resp.getErrorCodes()
                )
            );
        }

        // Captcha passed — proceed with registration logic
        log.info("Captcha verification succeeded for /api/auth/register, hostname={}", resp.getHostname());
        return ResponseEntity.ok(Map.of("message", "Captcha verified - proceed with registration"));
    }
}