package com.azarenka.evebuilders.service.api.market;

public interface IInventoryService {

    boolean hasSufficientStock(String username, String typeId, long qty);

    long getAvailableStock(String username, String typeId);
}
