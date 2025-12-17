package com.azarenka.evebuilders.domain.auth.auth.ui;

import java.util.List;

public record MeResponse(String username, List<String> roles) {
}
