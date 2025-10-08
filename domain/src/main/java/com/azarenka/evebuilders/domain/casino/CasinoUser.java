package com.azarenka.evebuilders.domain.casino;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "casino_user", schema = "builders")
public class CasinoUser {

    @Id
    @Column(length = 64, name = "character_id", unique = true, nullable = false)
    private Integer characterId;
    @Column(name = "character_name", unique = true, nullable = false)
    private String characterName;
    @Column(name = "count_points", nullable = false)
    private Integer countPoints;
    @Column(name = "create_date")
    private LocalDate createDate = LocalDate.now();
    @Column(name = "update_date")
    private LocalDate updateDate = LocalDate.now();

    public CasinoUser() {
    }

    public CasinoUser(Integer characterId, String characterName, Integer countPoints, LocalDate createDate,
                      LocalDate updateDate) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.countPoints = countPoints;
        this.createDate = createDate;
        this.updateDate = updateDate;
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

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CasinoUser that = (CasinoUser) o;

        return new EqualsBuilder()
            .append(characterId, that.characterId)
            .append(characterName, that.characterName)
            .append(countPoints, that.countPoints)
            .append(createDate, that.createDate)
            .append(updateDate, that.updateDate)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(characterId)
            .append(characterName)
            .append(countPoints)
            .append(createDate)
            .append(updateDate)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "CasinoUser{" +
            "  characterId=" + characterId +
            ", characterName='" + characterName + '\'' +
            ", countPoints=" + countPoints +
            ", createDate=" + createDate +
            ", updateDate=" + updateDate +
            '}';
    }
}
