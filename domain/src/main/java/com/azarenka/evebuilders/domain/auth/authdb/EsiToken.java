package com.azarenka.evebuilders.domain.auth.authdb;

public class EsiToken {

    private Long characterId;
    private String characterName;

    public EsiToken() {
    }

    public EsiToken(long characterId, String characterName) {
        this.characterId = characterId;
        this.characterName = characterName;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }
}
