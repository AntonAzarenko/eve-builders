package com.azarenka.evebuilders.service.impl.auth.eve;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.db.UserToken;
import com.azarenka.evebuilders.domain.dto.EveUserPrincipal;
import com.azarenka.evebuilders.domain.enums.AAGroupsEnum;
import com.azarenka.evebuilders.service.api.IAuthIntegrationService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.AllianceAuthService;
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
import java.time.ZoneId;
import java.util.HashSet;
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
    private IUserTokenService tokenService;
    @Autowired
    private EveCharacterService eveCharacterService;
    @Autowired
    private IAuthIntegrationService authIntegrationService;
    @Autowired
    private Environment env;

    @Autowired
    private AllianceAuthService allianceAuthService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        var attributes = oAuth2User.getAttributes();
        String characterName = (String) attributes.get("CharacterName");
        String characterId = attributes.get("CharacterID").toString();
        User user = authenticateByAccessToken(
            userRequest.getAccessToken().getTokenValue(),
            characterName,
            characterId,
            Locale.US
        );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EveUserPrincipal currentPrincipal) {
            return currentPrincipal;
        }
        LOGGER.info("User {} authenticated {}", user.getUsername(), LocalDateTime.now());
        return new EveUserPrincipal(user, attributes);
    }

    public User authenticateByAccessToken(String accessToken) {
        String characterId = eveCharacterService.getCharacterIdFromToken(accessToken);
        String characterName = eveCharacterService.getCharacterNameFromToken(accessToken);
        return authenticateByAccessToken(accessToken, characterName, characterId, Locale.US);
    }

    public User authenticateByAccessToken(String accessToken, String characterName, String characterId, Locale locale) {
        Optional<User> existingUser = userService.getByUsername(characterName);
        User user = existingUser.orElseGet(() ->
            buildUserFromToken(accessToken, characterName, characterId, locale));
        if (!checkAuth(user)) {
            LOGGER.info("User {} doesn't have permissions", characterName);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user", "User does not have permissions", "")
            );
        }
        defineRole(user);
        userService.saveUser(user);
        return user;
    }

    private User buildUserFromToken(String accessToken, String characterName, String characterId, Locale locale) {
        var user = new User();
        var characterInfoJson = eveCharacterService.getCharacterInfo(accessToken, characterId);
        var userId = UUID.randomUUID().toString();
        LOGGER.info("Authenticating user {}. CharacterInfo={} ",characterName, characterInfoJson);
        user.setUid(userId);
        user.setUsername(characterName);
        user.setCharacterId(characterId);
        user.setCharacterInfo(characterInfoJson);
        user.setPassword("");
        user.setCorporationName(eveCharacterService.getCharacterCorporationName(characterInfoJson));
        user.setAllianceName(eveCharacterService.getCharacterAllianceName(characterInfoJson));
        user.setMainCharacter(true);
        user.setLanguage(locale.getLanguage());
        user.setTheme("dark");
        user.setEnabled(true);
        var userName = SecurityUtils.getUserName();
        if (Objects.nonNull(userName)) {
            userService.getByUsername(userName).ifPresent(mainUser -> {
                user.setMainId(mainUser.getUid());
                user.setRoles(mainUser.getRoles());
                user.setMainCharacter(false);
                LOGGER.info("User [{}] was add as a character to [{}]", user.getUsername(), mainUser.getUsername());
                //createToken(userRequest, user);
            });
        }
        return user;
    }

    public boolean checkAuth(User user) {
        if (env.acceptsProfiles(Profiles.of("prod"))) {
            var groupIdsByUsername = allianceAuthService.findGroupIdsByUsername(user.getUsername());
            LOGGER.info("User {} has groups {}", user.getUsername(), groupIdsByUsername);
            var isAdminGroup = checkAdminGroup(groupIdsByUsername, user);
            var isIndustryGroup = checkIndustryGroup(groupIdsByUsername, user);
            var isMiningGroup = checkMiningGroup(groupIdsByUsername, user);
            return/* ALLIANCE_NAMES.contains(user.getAllianceName())
                &&*/ isIndustryGroup
                || isMiningGroup
                || isAdminGroup;
        } else {
            return true;
        }
    }

    private boolean checkIndustryGroup(List<Integer> groupIdsByUsername, User user) {
        boolean contains = groupIdsByUsername.contains(AAGroupsEnum.INDUSTRY.getGroupId());
        if (contains) {
            var roles = user.getRoles();
            if (Objects.isNull(roles)) roles = new HashSet<>();
            roles.add(Role.ROLE_BUILDER);
            user.setRoles(roles);
        }
        return contains;
    }

    private boolean checkMiningGroup(List<Integer> groupIdsByUsername, User user) {
        boolean contains = groupIdsByUsername.contains(AAGroupsEnum.MINING.getGroupId());
        if (contains) {
            var roles = user.getRoles();
            if (Objects.isNull(roles)) roles = new HashSet<>();
            roles.add(Role.ROLE_MINER);
            user.setRoles(roles);
        }
        return contains;
    }

    private boolean checkAdminGroup(List<Integer> groupIdsByUsername, User user) {
        boolean contains = groupIdsByUsername.contains(AAGroupsEnum.DEPARTMENT_OF_INDUSTRY.getGroupId());
        if (contains) {
            var roles = user.getRoles();
            if (Objects.isNull(roles)) roles = new HashSet<>();
            roles.add(Role.ROLE_ADMIN);
            roles.add(Role.ROLE_SUPER_ADMIN);
            user.setRoles(roles);
        }
        return contains;
    }

    private void defineRole(User user) {
        if (Objects.isNull(user.getRoles()) || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(Role.ROLE_VIEWER));
        }
    }

    private void createToken(OAuth2UserRequest userRequest, User user) {
        var userId = user.getUid();
        var accessToken = userRequest.getAccessToken().getTokenValue();
        var expiresAt = userRequest.getAccessToken().getExpiresAt();
        var token = new UserToken();
        token.setUserId(userId);
        token.setAccessToken(accessToken);
        //token.setRefreshToken(userRequest.getRefreshToken().getTokenValue());
        token.setExpiresAt(LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault()));
        tokenService.save(token);
        LOGGER.info("User [{}], Token updated", user.getUsername());
    }
}
