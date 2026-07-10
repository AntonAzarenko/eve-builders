package com.azarenka.evebuilders.service.api;

public interface IEveCharacterService {

    String getCharacterInfo(String accessToken, String characterId);

    String getCharacterIdFromToken(String accessToken);

    String getCharacterNameFromToken(String accessToken);

    String getCharacterCorporationName(String json);

    String getCharacterAllianceName(String json);

}
