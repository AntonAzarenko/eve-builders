package com.azarenka.evebuilders.domain.casino.dto;

import com.azarenka.evebuilders.domain.casino.Box;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserInfo userInfo = (UserInfo) o;

        return new EqualsBuilder().append(characterId, userInfo.characterId)
            .append(characterName, userInfo.characterName)
            .append(countPoints, userInfo.countPoints)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(characterId).append(characterName).append(countPoints).toHashCode();
    }

    @Override
    public String toString() {
        return "UserInfo{" +
            "characterId=" + characterId +
            ", characterName='" + characterName + '\'' +
            ", countPoints=" + countPoints +
            '}';
    }
}
