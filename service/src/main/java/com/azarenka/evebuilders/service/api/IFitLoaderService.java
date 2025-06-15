package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Fit;

import java.util.List;

public interface IFitLoaderService {

    //TODO adjust logic to return Object with error messages instead of boolean
    boolean upload(String text);

    List<Fit> getAll();

    Fit getFitById(String id);

    boolean updateFit(Fit fit);
}
