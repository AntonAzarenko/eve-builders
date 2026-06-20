package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.auth.auth.ui.CurrentUserProfileResponse;

public interface IProfileService {

    CurrentUserProfileResponse getCurrentProfile();

    void updateLanguage(String language);

    void updateTheme(String themeName);
}
