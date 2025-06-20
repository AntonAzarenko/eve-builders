package com.azarenka.evebuilders.service.impl.auth;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.service.api.IAuthIntegrationService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.intergarion.EveCharacterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EveOAuth2UserService extends DefaultOAuth2UserService {

    private static final List<String> ALLIANCE_NAMES = List.of("HOLD MY PROBS", "Intrepid Crossing");
    private static final Logger LOGGER = LoggerFactory.getLogger(EveOAuth2UserService.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private EveCharacterService eveCharacterService;
    @Autowired
    private IAuthIntegrationService authIntegrationService;
    @Autowired
    private Environment env;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        var attributes = oAuth2User.getAttributes();
        String characterName = (String) attributes.get("CharacterName");
        Optional<User> existingUser = userService.getByUsername(characterName);
        User user = existingUser.orElseGet(() ->
            getUserBasedOnTokenResponse(userRequest, attributes, Locale.US));
        if (!checkAuth(user)) {
            LOGGER.info("User {} doesn't have permissions", characterName);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user", "User does not have permissions", "")
            );
        }
        userService.saveUser(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EveUserPrincipal currentPrincipal) {
            return currentPrincipal;
        }
        LOGGER.info("User {} authenticated {}", user.getUsername(), LocalDateTime.now());
        return new EveUserPrincipal(user, attributes);
    }

    private User getUserBasedOnTokenResponse(OAuth2UserRequest userRequest, Map<String, Object> attributes,
                                             Locale locale) {
        var user = new User();
        var accessToken = userRequest.getAccessToken().getTokenValue();
        var characterId = attributes.get("CharacterID").toString();
        var characterName = (String) attributes.get("CharacterName");
        var characterInfoJson = eveCharacterService.getCharacterInfo(accessToken, characterId);
        var userId = UUID.randomUUID().toString();
        user.setUid(userId);
        user.setUsername(characterName);
        user.setCharacterId(characterId);
        user.setCharacterInfo(characterInfoJson);
        user.setPassword("");
        user.setCorporationName(eveCharacterService.getCharacterCorporationName(accessToken));
        user.setAllianceName(eveCharacterService.getCharacterAllianceName(accessToken));
        user.setMainCharacter(true);
        defineRole(characterName, user);
        user.setLanguage(locale.getLanguage());
        var userName = SecurityUtils.getUserName();
        if (Objects.nonNull(userName)) {
            userService.getByUsername(userName).ifPresent(mainUser -> {
                user.setMainId(mainUser.getUid());
                user.setRoles(mainUser.getRoles());
                LOGGER.info("User [{}] was add as a character to [{}]", user.getUsername(), mainUser.getUsername());
            });
        }
        return user;
    }

    public boolean checkAuth(User user) {
        if (env.acceptsProfiles(Profiles.of("prod"))) {
            return ALLIANCE_NAMES.contains(user.getAllianceName())
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
