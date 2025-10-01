package com.azarenka.evebuilders.domain.casino;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "casino_reward", schema = "builders")
public class Reward {

    @Id
    @Column(length = 64)
    private String uid;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "value", nullable = false)
    private String value;
    @Column(name = "box_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BoxTypeEnum boxType;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BoxTypeEnum getBoxType() {
        return boxType;
    }

    public void setBoxType(BoxTypeEnum boxType) {
        this.boxType = boxType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Reward reward = (Reward) o;

        return new EqualsBuilder().append(uid, reward.uid)
            .append(title, reward.title)
            .append(value, reward.value)
            .append(boxType, reward.boxType)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uid).append(title).append(value).append(boxType).toHashCode();
    }

    @Override
    public String toString() {
        return "Reward{" +
            "uid='" + uid + '\'' +
            ", title='" + title + '\'' +
            ", value=" + value +
            ", boxType=" + boxType +
            '}';
    }
}
