package com.azarenka.evebuilders.domain.casino;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rewarded_fleet", schema = "builders")
public class RewardedFleet {

    @Id
    @Column(length = 64)
    private String uid;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "fleetId", nullable = false)
    private Integer fleetId;
    @Column(name = "hash", nullable = false)
    private String hash;
    @Column(name = "is_rewarded", nullable = false)
    private Boolean isRewarded;
    @Column(name = "count_rewarded")
    private Integer countRewarded;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getFleetId() {
        return fleetId;
    }

    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getRewarded() {
        return isRewarded;
    }

    public void setRewarded(Boolean rewarded) {
        isRewarded = rewarded;
    }

    public Integer getCountRewarded() {
        return countRewarded;
    }

    public void setCountRewarded(Integer countRewarded) {
        this.countRewarded = countRewarded;
    }
}
