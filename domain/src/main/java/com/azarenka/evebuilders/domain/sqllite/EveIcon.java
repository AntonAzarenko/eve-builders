package com.azarenka.evebuilders.domain.sqllite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eveIcons")
public class EveIcon {

    @Id
    @Column(name = "iconID")
    private Integer iconId;

    @Column(name = "iconFile", nullable = false)
    private String iconFile;

    @Column(name = "description", nullable = true)
    private String description;

    public EveIcon() {}

    public EveIcon(Integer iconId, String iconFile, String description) {
        this.iconId = iconId;
        this.iconFile = iconFile;
        this.description = description;
    }

    public Integer getIconId() {
        return iconId;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public String getIconFile() {
        return iconFile;
    }

    public void setIconFile(String iconFile) {
        this.iconFile = iconFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "EveIcon{" +
                "iconId=" + iconId +
                ", iconFile='" + iconFile + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
