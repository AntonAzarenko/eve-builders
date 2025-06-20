package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.TokenResponse;
import com.vaadin.flow.component.html.Image;

public interface IEveAuthService {

    String generateAuthUrl();

    TokenResponse exchangeCodeForToken(String authorizationCode);

    Image getCharacterPortrait();

    Image getCharacterPortrait128();

    String getAppVersion();
}
