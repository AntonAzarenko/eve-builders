package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;

import java.util.List;

public interface ICorporationService {

    ManagedCorporation addCorporation(String corporationName);

    List<ManagedCorporation> getMyCorporations();

    List<ManagedCorporation> getAllCorporations();
}
