package com.azarenka.evebuilders.config.database;

import java.util.Objects;
import java.util.regex.Pattern;

final class DatabaseIdentifierValidator {

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private DatabaseIdentifierValidator() {
    }

    static String requireSafeIdentifier(String identifier, String propertyName) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalStateException(propertyName + " must be provided");
        }
        if (!SAFE_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalStateException(propertyName + " contains unsupported characters: " + identifier);
        }
        return identifier;
    }

    static String quoteIdentifier(String identifier) {
        Objects.requireNonNull(identifier, "identifier");
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
