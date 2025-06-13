package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.service.api.IEveAuthService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@RestController
public class EveAuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveAuthController.class);

    @Autowired
    private IEveAuthService eveAuthService;
    @Autowired
    private AuthService authService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserTokenService userTokenService;

    @GetMapping("/auth/eve/callback")
    public RedirectView handleCallback(@RequestParam("code") String code, HttpServletRequest request) {
        String userName = SecurityUtils.getUserName();
        boolean isUserLoggedIn = Objects.nonNull(userName);
        var tokenResponse = eveAuthService.exchangeCodeForToken(code);
        var user = authService.processUser(tokenResponse, isUserLoggedIn, Locale.US);
        if (Objects.nonNull(user)) {
            if (!isUserLoggedIn) {
                userService.authenticateUser(user, request);
                var userToken = userTokenService.createUserToken(user.getUid(), tokenResponse);
                userTokenService.save(userToken);
                LOGGER.info("User {} authenticated {}", user.getUsername(), LocalDateTime.now());
            }
            return new RedirectView("/landing");
        } else {
            return new RedirectView("/unauthorized");
        }
    }
}
