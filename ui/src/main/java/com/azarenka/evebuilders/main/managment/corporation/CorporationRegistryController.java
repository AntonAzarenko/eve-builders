package com.azarenka.evebuilders.main.managment.corporation;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.main.managment.api.ICorporationRegistryController;
import com.azarenka.evebuilders.service.api.ICorporationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CorporationRegistryController implements ICorporationRegistryController {

    @Autowired
    private ICorporationService corporationService;

    @Override
    public List<ManagedCorporation> getAllCorporations() {
        return corporationService.getAllCorporations();
    }
}
