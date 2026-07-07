package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.service.api.IFitLoaderService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/fits", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@Tag(name = "Fits")
public class FitRestController {

    private final IFitLoaderService fitLoaderService;

    public FitRestController(IFitLoaderService fitLoaderService) {
        this.fitLoaderService = fitLoaderService;
    }

    @GetMapping
    @PreAuthorize("!hasRole('VIEWER')")
    public List<Fit> getAllFits() {
        return fitLoaderService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("!hasRole('VIEWER')")
    public Fit getFitById(@PathVariable String id) {
        Fit fit = fitLoaderService.getFitById(id);
        if (fit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fit not found: " + id);
        }
        return fit;
    }

    @PostMapping(value = "/upload", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("@accessControlSecurity.can('CONTRACTS_CREATE')")
    public boolean uploadFit(@RequestBody String text) {
        return fitLoaderService.upload(text);
    }
}
