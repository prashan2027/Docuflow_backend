package com.docuflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Provides endpoints for checking authentication status
 *
 * This controller handles the /api/auth/me endpoint that your React app
 * calls in the useEffect to check if user is already logged in
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * ✅ Check if user is authenticated (used by React on page load)
     *
     * Your React app calls this in useEffect:
     * fetch("http://localhost:8080/api/auth/me", { credentials: "include" })
     *
     * Returns:
     * - 200 OK with user details if authenticated
     * - 401 Unauthorized if not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated and not anonymous
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {

            Map<String, String> error = new HashMap<>();
            error.put("error", "Not authenticated");
            error.put("message", "Please login to continue");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Return user details
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", authentication.getName());
        userDetails.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        userDetails.put("authenticated", true);

        return ResponseEntity.ok(userDetails);
    }

    /**
     * ✅ Health check endpoint (optional)
     * Useful for monitoring if the auth service is running
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Authentication service is running");
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Session expired handler (optional)
     * Called when session expires and user tries to access protected resource
     */
    @GetMapping("/session-expired")
    public ResponseEntity<Map<String, String>> sessionExpired() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Session Expired");
        response.put("message", "Your session has expired. Please login again.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}