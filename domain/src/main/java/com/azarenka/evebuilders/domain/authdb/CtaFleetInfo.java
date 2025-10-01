package com.azarenka.evebuilders.domain.authdb;

import java.time.LocalDateTime;

public class CtaFleetInfo {

    private Integer id;
    private LocalDateTime createdAt;
    private String fleetName;
    private String hash;
    private Integer creatorId;
    private String doctrine;
    private String fleetType;

    public CtaFleetInfo() {
    }

    public CtaFleetInfo(String doctrine, Integer id, LocalDateTime createdAt, String fleetName, String hash,
                        Integer creatorId, String fleetType) {
        this.doctrine = doctrine;
        this.id = id;
        this.createdAt = createdAt;
        this.fleetName = fleetName;
        this.hash = hash;
        this.creatorId = creatorId;
        this.fleetType = fleetType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFleetName() {
        return fleetName;
    }

    public void setFleetName(String fleetName) {
        this.fleetName = fleetName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public String getDoctrine() {
        return doctrine;
    }

    public void setDoctrine(String doctrine) {
        this.doctrine = doctrine;
    }

    public String getFleetType() {
        return fleetType;
    }

    public void setFleetType(String fleetType) {
        this.fleetType = fleetType;
    }
}
