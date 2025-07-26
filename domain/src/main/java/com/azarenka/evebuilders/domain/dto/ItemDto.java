package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.db.Asset;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ItemDto {
    private EveIcon eveIcon;
    private InvType invType;
    private Asset asset;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemDto itemDto = (ItemDto) o;

        return new EqualsBuilder().append(eveIcon, itemDto.eveIcon)
            .append(invType, itemDto.invType)
            .append(asset, itemDto.asset)
            .append(userName, itemDto.userName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(eveIcon).append(invType).append(asset).append(userName).toHashCode();
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public EveIcon getEveIcon() {
        return eveIcon;
    }

    public void setEveIcon(EveIcon eveIcon) {
        this.eveIcon = eveIcon;
    }

    public InvType getInvType() {
        return invType;
    }

    public void setInvType(InvType invType) {
        this.invType = invType;
    }
}
