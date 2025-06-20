package com.azarenka.evebuilders.config;

import com.azarenka.evebuilders.service.impl.auth.EveOAuth2UserService;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final EveOAuth2UserService eveOAuth2UserService;

    public SecurityConfig(EveOAuth2UserService eveOAuth2UserService) {
        this.eveOAuth2UserService = eveOAuth2UserService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                new AntPathRequestMatcher("/login/oauth2/code/eveonline"),
                new AntPathRequestMatcher("/unauthorized"),
                new AntPathRequestMatcher("/api/**", "POST")
            ).permitAll()
        );
        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(eveOAuth2UserService)
            )
            .failureHandler(customFailureHandler())
            .defaultSuccessUrl("/landing", true)
        );
        http.logout(logout -> logout.logoutSuccessUrl("/logout"));
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/api/**"),
                new AntPathRequestMatcher("/unauthorized")
            )
        );
        super.configure(http);
    }

    @Bean
    public AuthenticationFailureHandler customFailureHandler() {
        return (request, response, exception) -> response.sendRedirect("/unauthorized");
    }
}
