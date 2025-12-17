package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.LocationInfo;
import com.azarenka.evebuilders.service.impl.UserService;
import com.azarenka.evebuilders.service.impl.UserTokenService;
import com.azarenka.evebuilders.service.impl.auth.eve.TokenRefreshService;
import com.azarenka.evebuilders.service.impl.intergarion.LocationIntegrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private UserService userService;
    @Autowired
    private UserTokenService tokenService;
    @Autowired
    private LocationIntegrationService locationIntegrationService;
    @Autowired
    private TokenRefreshService tokenRefreshService;

    public LocationInfo getLocationInfo(Long location, String userName) {
        Optional<User> optionalUser = userService.getByUsername(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            var updatedAccessToken = tokenRefreshService
                .refreshTokenIfNeeded(user.getUid())
                .defaultIfEmpty(tokenService.getUserToken(user.getUid()))
                .block();
            return locationIntegrationService.resolveLocation(location, updatedAccessToken);
        }
        return null;
    }
}
