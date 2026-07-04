package com.azarenka.evebuilders.main.managment.api;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;

import java.util.List;

public interface ICorporationRegistryController {

    List<ManagedCorporation> getAllCorporations();
}
