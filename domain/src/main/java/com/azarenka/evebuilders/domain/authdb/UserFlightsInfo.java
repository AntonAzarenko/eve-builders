package com.azarenka.evebuilders.domain.authdb;

public class UserFlightsInfo {

    private final String username;
    private final Long characterId;
    private final Long appearancesCount;
    private final boolean teamspeakConnected;

    public UserFlightsInfo(String username, Long characterId, Long appearancesCount, boolean teamspeakConnected) {
        this.username = username;
        this.characterId = characterId;
        this.appearancesCount = appearancesCount;
        this.teamspeakConnected = teamspeakConnected;
    }

    public String getUsername() {
        return username;
    }

    public Long getAppearancesCount() {
        return appearancesCount;
    }

    public boolean isTeamspeakConnected() {
        return teamspeakConnected;
    }

    public Long getCharacterId() {
        return characterId;
    }
}
