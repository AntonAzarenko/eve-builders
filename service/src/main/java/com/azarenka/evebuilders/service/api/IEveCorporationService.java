package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.mysql.Corporation;

public interface IEveCorporationService {

    Corporation getCorporation(String corpId);
}
