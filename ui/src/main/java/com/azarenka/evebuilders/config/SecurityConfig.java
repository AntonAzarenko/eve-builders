package com.azarenka.evebuilders.config;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;
import com.azarenka.evebuilders.service.impl.auth.eve.CookieAuthFilter;
import com.azarenka.evebuilders.service.impl.auth.eve.EveAuthenticationSuccessHandler;
import com.azarenka.evebuilders.service.impl.auth.eve.EveOAuth2UserService;
import com.azarenka.evebuilders.service.impl.auth.eve.LogoutSuccessHandler;
import com.azarenka.evebuilders.service.impl.auth.eve.ui.JwtAuthFilter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final LogoutSuccessHandler logoutSuccessHandler;
    private final EveOAuth2UserService eveOAuth2UserService;
    private final EveAuthenticationSuccessHandler eveAuthenticationSuccessHandler;
    private final CookieAuthFilter cookieAuthFilter;

    public SecurityConfig(EveOAuth2UserService eveOAuth2UserService,
                          EveAuthenticationSuccessHandler eveAuthenticationSuccessHandler,
                          CookieAuthFilter cookieAuthFilter,
                          LogoutSuccessHandler logoutSuccessHandler) {
        this.eveOAuth2UserService = eveOAuth2UserService;
        this.eveAuthenticationSuccessHandler = eveAuthenticationSuccessHandler;
        this.cookieAuthFilter = cookieAuthFilter;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }


    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        http
            .securityMatcher("/api/**")
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/api/auth/login"),
                    new AntPathRequestMatcher("/api/auth/eve/exchange"),
                    new AntPathRequestMatcher("/api/auth/me"),
                    new AntPathRequestMatcher("/api/auth/profile"),
                    new AntPathRequestMatcher("/api/swagger-ui"),
                    new AntPathRequestMatcher("/api/swagger-ui/**"),
                    new AntPathRequestMatcher("/api/swagger-ui.html"),
                    new AntPathRequestMatcher("/api/swagger-ui.html/**"),
                    new AntPathRequestMatcher("/api/v3/api-docs"),
                    new AntPathRequestMatcher("/api/v3/api-docs/**"),
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/api/ping"),
                    new AntPathRequestMatcher("/api/auth/refresh"),
                    new AntPathRequestMatcher("/api/auth/logout"))
                .permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> response.sendRedirect("/unauthorized");
    }
}
