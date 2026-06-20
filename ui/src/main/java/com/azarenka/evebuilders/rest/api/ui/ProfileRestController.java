package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.CurrentUserProfileResponse;
import com.azarenka.evebuilders.domain.auth.auth.ui.ProfileLanguageRequest;
import com.azarenka.evebuilders.domain.auth.auth.ui.ProfileThemeRequest;
import com.azarenka.evebuilders.service.api.IProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileRestController {

    private final IProfileService profileService;

    public ProfileRestController(IProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public CurrentUserProfileResponse getCurrentProfile() {
        return profileService.getCurrentProfile();
    }

    @PutMapping("/language")
    public ResponseEntity<Void> updateLanguage(@RequestBody ProfileLanguageRequest request) {
        if (request == null || request.language() == null || request.language().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language is required");
        }
        profileService.updateLanguage(request.language());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/theme")
    public ResponseEntity<Void> updateTheme(@RequestBody ProfileThemeRequest request) {
        if (request == null || request.theme() == null || request.theme().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "theme is required");
        }
        profileService.updateTheme(request.theme());
        return ResponseEntity.noContent().build();
    }
}
