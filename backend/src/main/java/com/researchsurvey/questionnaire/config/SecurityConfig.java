package com.researchsurvey.questionnaire.config;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService adminUsers(
            PasswordEncoder encoder,
            @Value("${survey.admin.username}") String adminUsername,
            @Value("${survey.admin.password}") String adminPassword,
            @Value("${survey.analyst.username}") String analystUsername,
            @Value("${survey.analyst.password}") String analystPassword) {
        if (adminUsername.isBlank() || analystUsername.isBlank() || adminUsername.equals(analystUsername)) {
            throw new IllegalStateException("관리자와 분석가 계정 이름은 서로 다른 값으로 설정해야 합니다.");
        }
        if (adminPassword.length() < 12 || analystPassword.length() < 12) {
            throw new IllegalStateException("관리자 비밀번호는 12자 이상이어야 합니다.");
        }
        var admin = User.withUsername(adminUsername).password(encoder.encode(adminPassword)).roles("ADMIN", "ANALYST").build();
        var analyst = User.withUsername(analystUsername).password(encoder.encode(analystPassword)).roles("ANALYST").build();
        return new InMemoryUserDetailsManager(admin, analyst);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/v1/submissions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/analytics/**").hasAnyRole("ADMIN", "ANALYST")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .build();
    }
}
