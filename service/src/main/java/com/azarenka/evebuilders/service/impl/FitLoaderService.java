package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.mysql.Fit;
import com.azarenka.evebuilders.repository.mysql.IFitRepository;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.util.FitConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class FitLoaderService implements IFitLoaderService {

    @Autowired
    private FitConverter converter;
    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private IFitRepository fitRepository;

    @Override
    public boolean upload(String text) {
        Fit fit = converter.convertFromText(text);
        if (Objects.isNull(fit)) {
            return false;
        }
        fitRepository.saveAndFlush(fit);
        return true;
    }

    @Override
    public List<Fit> getAll() {
        return fitRepository.findAll();
    }

    @Override
    public Fit getFitById(String id) {
        return fitRepository.findById(id).orElse(null);
    }
}
