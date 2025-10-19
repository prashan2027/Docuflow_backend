package com.docuflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * Custom authentication filter that accepts JSON payloads
 * instead of form parameters (x-www-form-urlencoded)
 *
 * This matches your React app's JSON request:
 * fetch("/api/auth/login", {
 *   method: "POST",
 *   headers: { "Content-Type": "application/json" },
 *   body: JSON.stringify({ username, password })
 * })
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // Only process POST requests
        if (!request.getMethod().equals("POST")) {
            throw new IllegalArgumentException("Authentication method not supported: " + request.getMethod());
        }

        try {
            // Read JSON body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // Parse JSON to Map
            @SuppressWarnings("unchecked")
            Map<String, String> credentials = objectMapper.readValue(sb.toString(), Map.class);

            String username = credentials.get("username");
            String password = credentials.get("password");

            // Validate
            if (username == null || password == null) {
                throw new IllegalArgumentException("Username and password must be provided");
            }

            // Trim whitespace
            username = username.trim();

            // Create authentication token
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Allow subclasses to set the "details" property
            setDetails(request, authRequest);

            // Authenticate using the AuthenticationManager
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse authentication request body", e);
        }
    }
}