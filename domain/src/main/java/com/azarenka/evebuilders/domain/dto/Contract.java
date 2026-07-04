package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contract {
    @JsonProperty("contract_id")
    private long contractId;

    @JsonProperty("issuer_id")
    private long issuerId;

    @JsonProperty("issuer_corporation_id")
    private long issuerCorporationId;

    @JsonProperty("assignee_id")
    private long assigneeId;

    @JsonProperty("acceptor_id")
    private long acceptorId;

    @JsonProperty("availability")
    private String availability;

    @JsonProperty("for_corporation")
    private boolean forCorporation;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("date_issued")
    private String dateIssued;

    @JsonProperty("date_expired")
    private String dateExpired;

    @JsonProperty("start_location_id")
    private long startLocationId;

    @JsonProperty("end_location_id")
    private long endLocationId;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("reward")
    private Double reward;

    @JsonProperty("collateral")
    private Double collateral;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public long getContractId() {
        return contractId;
    }

    public void setContractId(long contractId) {
        this.contractId = contractId;
    }

    public long getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(long issuerId) {
        this.issuerId = issuerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getIssuerCorporationId() {
        return issuerCorporationId;
    }

    public void setIssuerCorporationId(long issuerCorporationId) {
        this.issuerCorporationId = issuerCorporationId;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public long getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(long acceptorId) {
        this.acceptorId = acceptorId;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isForCorporation() {
        return forCorporation;
    }

    public void setForCorporation(boolean forCorporation) {
        this.forCorporation = forCorporation;
    }

    public String getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(String dateIssued) {
        this.dateIssued = dateIssued;
    }

    public String getDateExpired() {
        return dateExpired;
    }

    public void setDateExpired(String dateExpired) {
        this.dateExpired = dateExpired;
    }

    public long getStartLocationId() {
        return startLocationId;
    }

    public void setStartLocationId(long startLocationId) {
        this.startLocationId = startLocationId;
    }

    public long getEndLocationId() {
        return endLocationId;
    }

    public void setEndLocationId(long endLocationId) {
        this.endLocationId = endLocationId;
    }

    public Double getReward() {
        return reward;
    }

    public void setReward(Double reward) {
        this.reward = reward;
    }

    public Double getCollateral() {
        return collateral;
    }

    public void setCollateral(Double collateral) {
        this.collateral = collateral;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Contract contract = (Contract) o;

        return new EqualsBuilder().append(contractId, contract.contractId)
            .append(issuerId, contract.issuerId)
            .append(issuerCorporationId, contract.issuerCorporationId)
            .append(assigneeId, contract.assigneeId)
            .append(acceptorId, contract.acceptorId)
            .append(forCorporation, contract.forCorporation)
            .append(startLocationId, contract.startLocationId)
            .append(endLocationId, contract.endLocationId)
            .append(availability, contract.availability)
            .append(status, contract.status)
            .append(type, contract.type)
            .append(title, contract.title)
            .append(dateIssued, contract.dateIssued)
            .append(dateExpired, contract.dateExpired)
            .append(price, contract.price)
            .append(reward, contract.reward)
            .append(collateral, contract.collateral)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(contractId)
            .append(issuerId)
            .append(issuerCorporationId)
            .append(assigneeId)
            .append(acceptorId)
            .append(availability)
            .append(forCorporation)
            .append(status)
            .append(type)
            .append(title)
            .append(dateIssued)
            .append(dateExpired)
            .append(startLocationId)
            .append(endLocationId)
            .append(price)
            .append(reward)
            .append(collateral)
            .toHashCode();
    }
}
