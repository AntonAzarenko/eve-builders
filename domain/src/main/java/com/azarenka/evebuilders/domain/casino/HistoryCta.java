package com.azarenka.evebuilders.domain.casino;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "history_points_cta", schema = "builders")
public class HistoryCta {

    @Id
    @Column(length = 64, unique = true, nullable = false)
    private String id;
    @Column(length = 64, name = "character_id", nullable = false)
    private Integer characterId;
    @Column(name = "character_name", nullable = false)
    private String characterName;
    @Column(name = "fleet_name", nullable = false)
    private String fleetName;
    @Column(name = "points", nullable = false)
    private Integer points;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryCta that = (HistoryCta) o;

        return new EqualsBuilder().append(id, that.id)
            .append(characterId, that.characterId)
            .append(characterName, that.characterName)
            .append(fleetName, that.fleetName)
            .append(points, that.points)
            .append(eventDate, that.eventDate)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(characterId)
            .append(characterName)
            .append(fleetName)
            .append(points)
            .append(eventDate)
            .toHashCode();
    }
}
