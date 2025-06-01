package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Corporation;

public interface IEveCorporationService {

    Corporation getCorporation(String corpId);
}
