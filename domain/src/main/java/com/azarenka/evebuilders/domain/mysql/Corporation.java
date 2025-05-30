package com.azarenka.evebuilders.domain.mysql;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Corporation {

    @JsonProperty("alliance_id")
    private Integer allianceId;

    @JsonProperty("ceo_id")
    private Integer ceoId;

    @JsonProperty("creator_id")
    private Integer creatorId;

    @JsonProperty("date_founded")
    private LocalDateTime dateFounded;

    @JsonProperty("description")
    private String description;

    @JsonProperty("faction_id")
    private Integer factionId;

    @JsonProperty("home_station_id")
    private Integer homeStationId;

    @JsonProperty("member_count")
    private Integer memberCount;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shares")
    private Long shares;

    @JsonProperty("tax_rate")
    private Float taxRate;

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("url")
    private String url;

    @JsonProperty("war_eligible")
    private Boolean warEligible;

    public Integer getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(Integer allianceId) {
        this.allianceId = allianceId;
    }

    public Integer getCeoId() {
        return ceoId;
    }

    public void setCeoId(Integer ceoId) {
        this.ceoId = ceoId;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getDateFounded() {
        return dateFounded;
    }

    public void setDateFounded(LocalDateTime dateFounded) {
        this.dateFounded = dateFounded;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFactionId() {
        return factionId;
    }

    public void setFactionId(Integer factionId) {
        this.factionId = factionId;
    }

    public Integer getHomeStationId() {
        return homeStationId;
    }

    public void setHomeStationId(Integer homeStationId) {
        this.homeStationId = homeStationId;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
        this.shares = shares;
    }

    public Float getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Float taxRate) {
        this.taxRate = taxRate;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getWarEligible() {
        return warEligible;
    }

    public void setWarEligible(Boolean warEligible) {
        this.warEligible = warEligible;
    }
}
