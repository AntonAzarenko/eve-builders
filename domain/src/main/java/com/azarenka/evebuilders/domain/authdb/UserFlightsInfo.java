package com.azarenka.evebuilders.domain.authdb;

public class UserFlightsInfo {

    private final String username;
    private final long characterId;
    private final long appearancesCount;
    private final boolean teamspeakConnected;

    public UserFlightsInfo(String username, long characterId, long appearancesCount, boolean teamspeakConnected) {
        this.username = username;
        this.characterId = characterId;
        this.appearancesCount = appearancesCount;
        this.teamspeakConnected = teamspeakConnected;
    }

    public String getUsername() {
        return username;
    }

    public long getAppearancesCount() {
        return appearancesCount;
    }

    public boolean isTeamspeakConnected() {
        return teamspeakConnected;
    }

    public long getCharacterId() {
        return characterId;
    }
}
