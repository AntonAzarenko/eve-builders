package com.azarenka.evebuilders.domain.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "receivers", schema = "builders")
public class Receiver extends ApplicationProperties {

    @Id
    @Column(name = "res_id", unique = true, nullable = false)
    private String resId;
    @Column(name = "receiver", nullable = false)
    private String receiver;

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Override
    public String getProperty() {
        return receiver;
    }
}
