package com.azarenka.evebuilders.domain.casino.dto;

import com.azarenka.evebuilders.domain.casino.Box;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserInfo {

    @JsonProperty("characterId")
    private Integer characterId;
    @JsonProperty("characterName")
    private String characterName;
    @JsonProperty("countPoints")
    private Integer countPoints;

    public UserInfo() {
    }

    public UserInfo(Integer characterId, String characterName, Integer countPoints, List<Box> boxes) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.countPoints = countPoints;
    }

    public Integer getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public Integer getCountPoints() {
        return countPoints;
    }

    public void setCountPoints(Integer countPoints) {
        this.countPoints = countPoints;
    }
}
