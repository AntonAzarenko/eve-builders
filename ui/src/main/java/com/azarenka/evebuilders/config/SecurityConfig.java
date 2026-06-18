package com.azarenka.evebuilders.config;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;
import com.azarenka.evebuilders.service.impl.auth.eve.CookieAuthFilter;
import com.azarenka.evebuilders.service.impl.auth.eve.EveAuthenticationSuccessHandler;
import com.azarenka.evebuilders.service.impl.auth.eve.EveOAuth2UserService;
import com.azarenka.evebuilders.service.impl.auth.eve.LogoutSuccessHandler;
import com.azarenka.evebuilders.service.impl.auth.eve.ui.JwtAuthFilter;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig extends VaadinWebSecurity {

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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


   /* @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .addFilterBefore(cookieAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/login/oauth2/code/eveonline"),
                    new AntPathRequestMatcher("/unauthorized"),
                    new AntPathRequestMatcher("/api/**")
                ).permitAll()

            );
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(eveOAuth2UserService)
            )
            //.failureHandler(customFailureHandler())
            .successHandler(eveAuthenticationSuccessHandler)
        );
        http.logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
            .addLogoutHandler(logoutSuccessHandler)
            .logoutSuccessUrl("/"));
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/api/**"),
                new AntPathRequestMatcher("/unauthorized")
            )
        );
        super.configure(http);
    }*/

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> response.sendRedirect("/unauthorized");
    }
}
