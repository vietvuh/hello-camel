package vvu.centrauthz.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import vvu.centrauthz.utilities.JsonTools;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public record CdcEvent(
    CdcEventType event,
    Long timestamp,
    String kind,
    String key,
    JsonNode before,
    JsonNode after) {

    public CdcEvent {
        if (Objects.isNull(timestamp)) {
            timestamp = System.currentTimeMillis();
        }
    }

    public JsonNode toJson() {
        return JsonTools.mapper().valueToTree(this);
    }
}
