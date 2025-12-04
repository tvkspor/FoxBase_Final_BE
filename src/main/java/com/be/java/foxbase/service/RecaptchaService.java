package com.be.java.foxbase.service;

import com.be.java.foxbase.utils.RecaptchaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);
    private final RestTemplate restTemplate;
    private final String secret;
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public RecaptchaService(RestTemplate restTemplate, @Value("${recaptcha.secret:}") String secret) {
        this.restTemplate = restTemplate;
        this.secret = secret;
        if (this.secret == null || this.secret.isBlank()) {
            log.warn("reCAPTCHA secret is not configured (recaptcha.secret is empty). Verification requests will fail.");
        }
    }

    public RecaptchaResponse verify(String token) {
        if (token == null || token.isBlank()) {
            log.debug("Recaptcha token is empty/null");
            RecaptchaResponse r = new RecaptchaResponse();
            r.setSuccess(false);
            r.setErrorCodes(Collections.singletonList("missing-input-response"));
            return r;
        }

        if (secret == null || secret.isBlank()) {
            log.error("Recaptcha secret is not set; cannot verify token");
            RecaptchaResponse r = new RecaptchaResponse();
            r.setSuccess(false);
            r.setErrorCodes(Collections.singletonList("missing-input-secret"));
            return r;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("response", token);

        try {
            ResponseEntity<RecaptchaResponse> resp = restTemplate.postForEntity(VERIFY_URL, form, RecaptchaResponse.class);
            RecaptchaResponse body = resp.getBody();
            if (body == null) {
                log.warn("Empty response body from recaptcha verify API");
                RecaptchaResponse r = new RecaptchaResponse();
                r.setSuccess(false);
                r.setErrorCodes(Collections.singletonList("empty-response"));
                return r;
            }
            if (!body.isSuccess()) {
                log.info("reCAPTCHA verification failed: errors={} hostname={}", body.getErrorCodes(), body.getHostname());
            } else {
                log.debug("reCAPTCHA verification succeeded for hostname={}", body.getHostname());
            }
            return body;
        } catch (RestClientException e) {
            log.error("Error while calling reCAPTCHA verify API: {}", e.getMessage());
            RecaptchaResponse r = new RecaptchaResponse();
            r.setSuccess(false);
            r.setErrorCodes(Collections.singletonList("api-call-failed"));
            return r;
        }
    }

    public boolean verifySuccess(String token, String expectedHostname) {
        RecaptchaResponse r = verify(token);
        if (r == null) return false;
        if (!r.isSuccess()) return false;
        if (expectedHostname != null && r.getHostname() != null) {
            return expectedHostname.equals(r.getHostname());
        }
        return true;
    }
}
