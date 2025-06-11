package com.azarenka.evebuilders.domain.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Alliance {

    @JsonProperty("alliance_id")
    private Integer allianceId;
    @JsonProperty("creator_corporation_id")
    private Integer creatorCorporationId;
    @JsonProperty("creator_id")
    private Integer creatorId;
    @JsonProperty("date_founded")
    private LocalDateTime dateFounded;
    @JsonProperty("executor_corporation_id")
    private Integer executorCorporationId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ticker")
    private String ticker;

    public Integer getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(Integer allianceId) {
        this.allianceId = allianceId;
    }

    public Integer getCreatorCorporationId() {
        return creatorCorporationId;
    }

    public void setCreatorCorporationId(Integer creatorCorporationId) {
        this.creatorCorporationId = creatorCorporationId;
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

    public Integer getExecutorCorporationId() {
        return executorCorporationId;
    }

    public void setExecutorCorporationId(Integer executorCorporationId) {
        this.executorCorporationId = executorCorporationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
}
