package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.service.api.ISeatIntegrationService;
import org.springframework.stereotype.Service;

@Service
public class SeatIntegrationService implements ISeatIntegrationService {

    @Override
    public boolean checkAuth(User user) {
        return true;
    }
}
