package com.azarenka.evebuilders.security;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.impl.intergarion.EveCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final String ALLIANCE_NAME = "HOLD MY PROBS";

    @Autowired
    private IUserService userService;
    @Autowired
    private EveCharacterService eveCharacterService;
    @Autowired
    private IAuthIntegrationService authIntegrationService;
    @Autowired
    private Environment env;

    public User processUser(TokenResponse tokenResponse, boolean isUserLoggedIn, Locale locale) {
        var accessToken = tokenResponse.getAccessToken();
        var characterName = eveCharacterService.getCharacterNameFromToken(accessToken);
        Optional<User> existingUser = userService.getByUsername(characterName);
        User user = existingUser.orElseGet(() -> getUserBasedOnTokenResponse(tokenResponse, characterName, isUserLoggedIn,
                locale));
        if (checkAuth(user)) {
            return userService.saveUser(user);
        }
        return null;
    }

    private User getUserBasedOnTokenResponse(TokenResponse tokenResponse, String characterName, boolean isUserLoggedIn,
                                             Locale locale) {
        User user = new User();
        String accessToken = tokenResponse.getAccessToken();
        String characterId = eveCharacterService.getCharacterIdFromToken(accessToken);
        String characterInfoJson = eveCharacterService.getCharacterInfo(accessToken, characterId);
        String userId = UUID.randomUUID().toString();
        user.setUid(userId);
        user.setUsername(characterName);
        user.setCharacterId(characterId);
        user.setCharacterInfo(characterInfoJson);
        user.setPassword("");
        user.setCorporationName(eveCharacterService.getCharacterCorporationName(accessToken));
        user.setAllianceName(eveCharacterService.getCharacterAllianceName(accessToken));
        user.setMainCharacter(!isUserLoggedIn);
        defineRole(characterName, user);
        if (isUserLoggedIn) {
            String userName = SecurityUtils.getUserName();
            Optional<User> byUsername = userService.getByUsername(userName);
            byUsername.ifPresent(main -> {
                user.setMainId(main.getUid());
                user.setRoles(main.getRoles());
            });
        }
        user.setLanguage(locale.getLanguage());
        return user;
    }

    public boolean checkAuth(User user) {
        if (env.acceptsProfiles(Profiles.of("prod"))) {
            return user.getAllianceName().equalsIgnoreCase(ALLIANCE_NAME)
                    && checkIndustryGroup(user);
        } else {
            return true;
        }
    }

    private boolean checkIndustryGroup(User user) {
        return authIntegrationService.checkUser(user.getUsername());
    }

    private void defineRole(String userName, User user) {
        if (userName.equals("AntonFromEpam")) {
            user.setRoles(Set.of(Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN));
        } else {
            user.setRoles(Set.of(Role.ROLE_USER));
        }
    }
}
