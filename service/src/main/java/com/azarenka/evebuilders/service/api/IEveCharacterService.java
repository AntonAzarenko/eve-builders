package com.azarenka.evebuilders.service.api;

import com.vaadin.flow.component.html.Image;

public interface IEveCharacterService {

    String getCharacterInfo(String accessToken, String characterId);

    String getCharacterIdFromToken(String accessToken);

    String getCharacterNameFromToken(String accessToken);

    String getCharacterCorporationName(String accessToken);

    String getCharacterAllianceName(String accessToken);

}
