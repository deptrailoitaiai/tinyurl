package org.example.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // Cho phép tất cả path
                .allowedOrigins("*") // Cho phép tất cả origin
                .allowedMethods("*") // Cho phép tất cả method: GET, POST, PUT, DELETE, OPTIONS,...
                .allowedHeaders("*") // Cho phép tất cả header
                .allowCredentials(false) // Nếu muốn cho phép gửi cookie thì để true
                .maxAge(3600); // Cache preflight 1 giờ
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để test API
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Cho phép tất cả request
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // Tắt HTTP Basic Auth
                .formLogin(form -> form.disable()); // Tắt form login mặc định

        return http.build();
    }
}
