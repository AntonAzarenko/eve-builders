package com.azarenka.evebuilders.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "destination", schema = "builders")
public class Destination extends ApplicationProperties {

    @Id
    @Column(name = "dest_id", unique = true, nullable = false)
    private String destId;

    @Column(name = "destination", nullable = false)
    private String destination;

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getProperty() {
        return destination;
    }
}
