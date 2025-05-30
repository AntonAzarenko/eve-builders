package com.azarenka.evebuilders.domain.sqllite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "invGroups")
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvGroup {

    @Id
    @Column(name = "groupId")
    private Integer groupID;
    @Column(name = "categoryId")
    private Integer categoryID;
    @Column(name = "groupName")
    private String groupName;

    public Integer getGroupID() {
        return groupID;
    }

    public void setGroupID(Integer groupId) {
        this.groupID = groupId;
    }

    public Integer getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Integer categoryId) {
        this.categoryID = categoryId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InvGroup invGroup = (InvGroup) o;

        return new EqualsBuilder().append(groupID, invGroup.groupID).append(categoryID, invGroup.categoryID).append(groupName, invGroup.groupName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(groupID).append(categoryID).append(groupName).toHashCode();
    }
}
