package com.azarenka.evebuilders.main.orders.corporation.api;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;

import java.util.List;

public interface IManagedCorporationsController {

    ManagedCorporation addCorporation(String corporationName);

    List<ManagedCorporation> getMyCorporations();

    List<ManagedCorporation> getAllCorporations();
}
