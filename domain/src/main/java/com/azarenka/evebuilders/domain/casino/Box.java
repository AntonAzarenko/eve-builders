package com.azarenka.evebuilders.domain.casino;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "casino_box", schema = "builders")
public class Box {

    @Id
    @Column(length = 64)
    private String uid;
    @Column(name = "character_id", nullable = false)
    private Integer characterId;
    @Column(name = "reward_id", nullable = false)
    private String rewardId;
    @Column(name = "box_type")
    @Enumerated(EnumType.STRING)
    private BoxTypeEnum boxType;
    @Column(name = "create_date")
    private LocalDate createDate = LocalDate.now();
    @Column(name = "update_date")
    private LocalDate updateDate = LocalDate.now();
    @Column(name = "claimed")
    private Boolean claimed = false;

    public Box() {
    }

    public Box(String uid, Integer characterId, String rewardId, BoxTypeEnum boxType,Boolean claimed, LocalDate createDate) {
        this.uid = uid;
        this.characterId = characterId;
        this.rewardId = rewardId;
        this.boxType = boxType;
        this.claimed = claimed;
        this.createDate = createDate;
    }

    public Boolean getClaimed() {
        return claimed;
    }

    public void setClaimed(Boolean claimed) {
        this.claimed = claimed;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String reward) {
        this.rewardId = reward;
    }

    public BoxTypeEnum getBoxType() {
        return boxType;
    }

    public void setBoxType(BoxTypeEnum boxType) {
        this.boxType = boxType;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Box box = (Box) o;

        return new EqualsBuilder().append(uid, box.uid)
            .append(characterId, box.characterId)
            .append(rewardId, box.rewardId)
            .append(boxType, box.boxType)
            .append(createDate, box.createDate)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uid)
            .append(characterId)
            .append(rewardId)
            .append(boxType)
            .append(createDate)
            .toHashCode();
    }

    @Override
    public String toString() {
        return "Box{" +
            "uid='" + uid + '\'' +
            ", characterId='" + characterId + '\'' +
            ", reward='" + rewardId + '\'' +
            ", boxType=" + boxType +
            ", createDate=" + createDate +
            '}';
    }
}
