package vvu.centrauthz.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;

/**
 * Error record representing API error responses.
 * Generated from OpenAPI schema: #/components/schemas/Error
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public record Error(
        @NotNull
        @NotBlank
        String code,

        String message,

        Map<String, String> details
) {
}
