package com.azarenka.evebuilders.domain.db;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.time.LocalDate;

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
    @Column(name = "text_fit", columnDefinition = "VARCHAR", nullable = false)
    private String textFit;

    @Column(name = "created_by", nullable = false)
    @ColumnDefault("'SYSTEM'")
    private String createdBy = "System";
    @CreationTimestamp
    @ColumnDefault("'now()'")
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();
    @ColumnDefault("'SYSTEM'")
    @Column(name = "updated_by")
    private String updatedBy = "SYSTEM";
    @ColumnDefault("'now()'")
    @Column(name = "updated_date")
    @UpdateTimestamp
    private LocalDate updatedDate = LocalDate.now();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

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
