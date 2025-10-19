package com.docuflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // --------------------
    // LDAP Properties
    // --------------------
    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Value("${spring.ldap.username}")
    private String bindDn;

    @Value("${spring.ldap.password}")
    private String bindPassword;

    @Value("${app.ldap.userSearchBase:ou=users}")
    private String userSearchBase;

    @Value("${app.ldap.userSearchFilter:(uid={0})}")
    private String userSearchFilter;

    @Value("${app.ldap.groupSearchBase:ou=groups}")
    private String groupSearchBase;

    @Value("${app.ldap.groupSearchFilter:(uniqueMember={0})}")
    private String groupSearchFilter;

    // --------------------
    // CORS Properties
    // --------------------
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --------------------
    // 1️⃣ CORS Configuration (Allow credentials for session)
    // --------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true); // ✅ Critical for session cookies
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // --------------------
    // 2️⃣ LDAP Context
    // --------------------
    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(bindDn);
        contextSource.setPassword(bindPassword);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    // --------------------
    // 3️⃣ LDAP Authorities Populator
    // --------------------
    @Bean
    public DefaultLdapAuthoritiesPopulator authoritiesPopulator(LdapContextSource contextSource) {
        DefaultLdapAuthoritiesPopulator populator =
                new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        populator.setGroupSearchFilter(groupSearchFilter);
        populator.setGroupRoleAttribute("cn");
        populator.setRolePrefix("ROLE_");
        populator.setIgnorePartialResultException(true);
        populator.setSearchSubtree(true);
        populator.setConvertToUpperCase(true);
        return populator;
    }

    // --------------------
    // 4️⃣ LDAP Authentication Provider
    // --------------------
    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(
            LdapContextSource contextSource,
            DefaultLdapAuthoritiesPopulator authoritiesPopulator) {

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(new FilterBasedLdapUserSearch(
                userSearchBase, userSearchFilter, contextSource
        ));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
        provider.setAuthoritiesMapper(authoritiesMapper());
        return provider;
    }

    // --------------------
    // 5️⃣ Role Mapper (Singular role names)
    // --------------------
    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>();
            for (GrantedAuthority authority : authorities) {
                String role = authority.getAuthority()
                        .replace("ROLE_REVIEWERS", "ROLE_REVIEWER")
                        .replace("ROLE_SUBMITTERS", "ROLE_SUBMITTER")
                        .replace("ROLE_APPROVERS", "ROLE_APPROVER")
                        .replace("ROLE_ADMINS", "ROLE_ADMIN");
                mapped.add(new SimpleGrantedAuthority(role));
            }
            return mapped;
        };
    }

    // --------------------
    // 6️⃣ Authentication Manager
    // --------------------
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            LdapAuthenticationProvider provider) throws Exception {

        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(provider);
        return authBuilder.build();
    }

    // --------------------
    // 7️⃣ Custom JSON Authentication Filter
    // --------------------
    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonAuthenticationFilter(AuthenticationManager authManager) {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        filter.setAuthenticationManager(authManager);
        filter.setFilterProcessesUrl("/api/auth/login");

        // ✅ Success Handler - Returns JSON for React
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            Map<String, Object> data = new HashMap<>();
            data.put("username", authentication.getName());
            data.put("roles", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
            data.put("message", "Login successful");

            response.getWriter().write(objectMapper.writeValueAsString(data));
        });

        // ✅ Failure Handler - Returns JSON error for React
        filter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", "Invalid username or password");

            response.getWriter().write(objectMapper.writeValueAsString(error));
        });

        return filter;
    }

    // --------------------
    // 8️⃣ Session Event Publisher (for concurrent session control)
    // --------------------
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // --------------------
    // 9️⃣ Security Filter Chain
    // --------------------
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authManager,
            JsonUsernamePasswordAuthenticationFilter jsonAuthFilter) throws Exception {

        http
                .authenticationManager(authManager)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ CSRF disabled for JSON API (session cookies handle auth)
                .csrf(csrf -> csrf.disable())

                // ✅ Session Management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().changeSessionId() // Prevent session fixation attacks
                        .invalidSessionUrl("/api/auth/session-expired")
                        .maximumSessions(3)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/api/auth/session-expired")
                )

                // ✅ Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/actuator/**", "/health", "/api/auth/**").permitAll()
                        // Role-based endpoints
//                        .requestMatchers("/api/submitter/**").hasRole("SUBMITTER")
//                        .requestMatchers("/api/reviewer/**").hasRole("REVIEWER")
//                        .requestMatchers("/api/approver/**").hasRole("APPROVER")
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // All others require authentication
                        .anyRequest().permitAll()
                )

                // ✅ Add custom JSON authentication filter
                .addFilterAt(jsonAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ Logout Handler - Returns JSON for React
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\":\"Logout successful\"}");
                        })
                        .permitAll()
                )

                // ✅ Exception Handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Please login to continue\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"You don't have permission to access this resource\"}");
                        })
                )

                // ✅ Security Headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .xssProtection(xss -> xss.disable()) // Modern browsers handle this
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'"))
                );

        return http.build();
    }
}