package com.azarenka.evebuilders.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Contract {
    @JsonProperty("contract_id")
    private long contractId;

    @JsonProperty("issuer_id")
    private long issuerId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Contract contract = (Contract) o;

        return new EqualsBuilder()
                .append(contractId, contract.contractId)
                .append(issuerId, contract.issuerId).
                append(title, contract.title)
                .append(status, contract.status)
                .append(type, contract.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(contractId)
                .append(issuerId)
                .append(title)
                .append(status)
                .append(type)
                .toHashCode();
    }
}
