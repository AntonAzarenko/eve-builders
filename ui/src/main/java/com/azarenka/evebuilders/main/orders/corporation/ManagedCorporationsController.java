package com.azarenka.evebuilders.main.orders.corporation;

import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.main.orders.corporation.api.IManagedCorporationsController;
import com.azarenka.evebuilders.service.api.ICorporationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManagedCorporationsController implements IManagedCorporationsController {

    @Autowired
    private ICorporationService corporationService;

    @Override
    public ManagedCorporation addCorporation(String corporationName) {
        return corporationService.addCorporation(corporationName);
    }

    @Override
    public List<ManagedCorporation> getMyCorporations() {
        return corporationService.getMyCorporations();
    }

    @Override
    public List<ManagedCorporation> getAllCorporations() {
        return corporationService.getAllCorporations();
    }
}
