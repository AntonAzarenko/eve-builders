package com.azarenka.evebuilders.domain.dto;

import com.azarenka.evebuilders.domain.mysql.Asset;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvType;

public class ItemDto {

    private EveIcon eveIcon;

    private InvType invType;

    private Asset asset;

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
