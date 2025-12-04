package com.be.java.foxbase.controller;

import com.be.java.foxbase.configuration.ZaloPayConfig;
import com.be.java.foxbase.dto.request.ZaloPayWebhookRequest;
import com.be.java.foxbase.service.ZaloPayWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zalopay")
@Slf4j
public class ZaloPayWebhookController {
    
    @Autowired
    private ZaloPayWebhookService webhookService;
    
    @Autowired
    private ZaloPayConfig zaloPayConfig;
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody ZaloPayWebhookRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String appTransId = request.getAppTransId();
        
        log.info("Received webhook for appTransId={} from IP={}", appTransId, clientIp);
        if (!zaloPayConfig.isIpAllowed(clientIp)) {
            log.warn("Webhook rejected: IP {} not in whitelist for appTransId={}", clientIp, appTransId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("IP not allowed");
        }
        
        try {
            webhookService.processWebhook(request, clientIp);
            return ResponseEntity.ok("success");
            
        } catch (Exception e) {
            log.error("Error processing webhook for appTransId={}: {}", appTransId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

