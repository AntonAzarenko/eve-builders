package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.mysql.User;

public interface ISeatIntegrationService {

    boolean checkAuth(User user);
}
