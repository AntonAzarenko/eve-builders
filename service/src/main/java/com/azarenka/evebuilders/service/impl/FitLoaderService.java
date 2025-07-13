package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.repository.database.IFitRepository;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.util.FitConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class FitLoaderService implements IFitLoaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FitLoaderService.class);

    @Autowired
    private FitConverter converter;
    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private IFitRepository fitRepository;

    @Override
    public boolean upload(String text) {
        var userName = SecurityUtils.getUserName();
        LOGGER.info("Upload fit. STARTED. LoadedBy={}", userName);
        var fit = converter.convertFromText(text);
        if (Objects.isNull(fit)) {
            return false;
        }
        fit.setCreatedBy(userName);
        fit.setUpdatedBy(userName);
        fit.setCreatedDate(LocalDate.now());
        fit.setUpdatedDate(LocalDate.now());
        fitRepository.saveAndFlush(fit);
        LOGGER.info("Upload fit. FINISHED. LoadedBy={}", userName);
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

    @Override
    public boolean updateFit(Fit fit) {
        String userName = SecurityUtils.getUserName();
        LOGGER.info("Update fit. STARTED. LoadedBy={}", userName);
        fit.setUpdatedBy(userName);
        fit.setUpdatedDate(LocalDate.now());
        fitRepository.saveAndFlush(fit);
        LOGGER.info("Update fit. FINISHED. LoadedBy={}", userName);
        return true;
    }
}
