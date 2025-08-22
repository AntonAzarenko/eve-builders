package com.azarenka.evebuilders.main.trade;

import com.azarenka.evebuilders.main.trade.api.IMarketRequestController;

import java.util.List;

public class MarketRequestController implements IMarketRequestController {
    @Override
    public List<String> getAllMinerals() {
        return List.of();
    }
}
