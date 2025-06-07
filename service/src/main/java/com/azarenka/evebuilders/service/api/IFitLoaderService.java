package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.Fit;

import java.util.List;

public interface IFitLoaderService {

    boolean upload(String text);

    List<Fit> getAll();

    Fit getFitById(String id);

    boolean updateFit(Fit fit);
}
