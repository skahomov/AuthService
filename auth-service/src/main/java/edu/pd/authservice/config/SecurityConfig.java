package edu.pd.authservice.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain authSecurity(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/login", "/api/auth/otp/verify", "/.well-known/jwks.json",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.ignoringRequestMatchers("/api/auth/login", "/api/auth/otp/verify",
                        "/api/auth/verify", "/api/auth/permissions/check"))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults())).build();
    }
}
