package com.azarenka.evebuilders.domain.mysql;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "fit", schema = "builders")
public class Fit {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "type_id")
    private Integer typeId;
    @Column(name = "group_id")
    private Integer groupId;
    @Column(name = "text_fit", columnDefinition = "TEXT", nullable = false)
    @Lob
    private String textFit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTextFit() {
        return textFit;
    }

    public void setTextFit(String textFit) {
        this.textFit = textFit;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
