package vvu.centrauthz.domains.helloworld.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Objects;

@Builder(toBuilder = true)
public record Ping(@NotBlank String message, Long timestamp) {
    public Ping {
        if (Objects.isNull(timestamp)) {
            timestamp = System.currentTimeMillis();
        }
    }

}
