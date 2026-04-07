package com.commerceos.authservice.api.rest;

import com.commerceos.authservice.application.handlers.TokenService;
import com.commerceos.authservice.infrastructure.persistence.ApiCredentialRepository;
import com.commerceos.authservice.domain.enums.CredentialStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final ApiCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/api/v1/auth/token")
    public ResponseEntity<?> token(
            @RequestParam String clientId,
            @RequestParam String clientSecret) {
        log.info("Token request received for: {}", clientId);
        try {
            String token = tokenService.issueToken(clientId, clientSecret);
            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "token_type", "Bearer"));
        } catch (Exception e) {
            log.error("Token error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/v1/auth/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/api/v1/auth/hash")
    public ResponseEntity<?> hash(@RequestParam String secret) {
        String encoded = passwordEncoder.encode(secret);
        return ResponseEntity.ok(Map.of("hash", encoded));
    }

    @GetMapping("/api/v1/auth/debug")
    public ResponseEntity<?> debug(@RequestParam String clientId,
                                    @RequestParam String secret) {
        var cred = credentialRepository
                .findByClientIdAndStatus(clientId, CredentialStatus.ACTIVE)
                .orElse(null);
        if (cred == null) {
            return ResponseEntity.ok(Map.of("found", false));
        }
        String storedHash = cred.getClientSecretHash();
        boolean matches = passwordEncoder.matches(secret, storedHash);
        return ResponseEntity.ok(Map.of(
                "found", true,
                "hashLength", storedHash.length(),
                "hashPrefix", storedHash.substring(0, 7),
                "matches", matches
        ));
    }
}
