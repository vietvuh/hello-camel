package vvu.centrauthz.utilities;

import lombok.experimental.UtilityClass;
import vvu.centrauthz.models.Error;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ErrorTools {
    public static Map<String, Object> toMap(Error e) {

        var map = new HashMap<String, Object>();
        map.put("code", e.code());
        Optional.ofNullable(e.message()).ifPresent(message -> map.put("message", message));
        Optional.ofNullable(e.details()).ifPresent(details -> map.put("details", details));

        return map;
    }

    private static final Pattern VALIDATION_PATTERN = Pattern.compile(
            "property: ([^;]+); value: [^;]*; constraint: ([^;]+);"
    );

    public static Error parseValidationError(String validationMessage) {
        Map<String, String> details = new HashMap<>();

        Matcher matcher = VALIDATION_PATTERN.matcher(validationMessage);
        while (matcher.find()) {
            String property = matcher.group(1).trim();
            String constraint = matcher.group(2).trim();
            details.put(property, constraint);
        }

        return Error.builder()
                .code("VALIDATION_FAILED")
                .message("Request validation failed")
                .details(details.isEmpty() ? null : details)
                .build();
    }
}
