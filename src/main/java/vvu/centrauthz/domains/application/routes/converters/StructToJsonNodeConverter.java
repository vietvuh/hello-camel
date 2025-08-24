package vvu.centrauthz.domains.application.routes.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import lombok.extern.slf4j.Slf4j;
import jakarta.enterprise.context.ApplicationScoped;
import vvu.centrauthz.utilities.JsonTools;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class StructToJsonNodeConverter implements TypeConverters {

    private final ObjectMapper objectMapper = JsonTools.mapper();

    @Converter
    public JsonNode structToJsonNode(Struct struct) {
        if (struct == null) {
            log.warn("Struct is null");
            return objectMapper.nullNode();
        }

        try {
            return convertStructToJsonNode(struct);
        } catch (Exception e) {
            log.error("Failed to convert Struct to JsonNode", e);
            return objectMapper.nullNode();
        }
    }

    private JsonNode convertStructToJsonNode(Struct struct) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        Schema schema = struct.schema();

        for (Field field : schema.fields()) {
            String fieldName = field.name();
            Object fieldValue = struct.get(fieldName);

            JsonNode jsonValue = convertValueToJsonNode(fieldValue, field.schema());
            objectNode.set(fieldName, jsonValue);
        }

        return objectNode;
    }

    private JsonNode convertValueToJsonNode(Object value, Schema schema) {
        if (value == null) {
            return objectMapper.nullNode();
        }

        return switch (schema.type()) {
            case STRING -> objectMapper.valueToTree(value.toString());
            case INT8, INT16, INT32 -> objectMapper.valueToTree(((Number) value).intValue());
            case INT64 -> objectMapper.valueToTree(((Number) value).longValue());
            case FLOAT32 -> objectMapper.valueToTree(((Number) value).floatValue());
            case FLOAT64 -> objectMapper.valueToTree(((Number) value).doubleValue());
            case BOOLEAN -> objectMapper.valueToTree((Boolean) value);
            case BYTES -> objectMapper.valueToTree(value);
            case STRUCT -> convertStructToJsonNode((Struct) value);
            case ARRAY -> convertArrayToJsonNode((List<?>) value, schema.valueSchema());
            case MAP -> convertMapToJsonNode((Map<?, ?>) value, schema.valueSchema());
            default -> {
                log.warn("Unsupported schema type: {}, converting as string", schema.type());
                yield objectMapper.valueToTree(value.toString());
            }
        };
    }

    private JsonNode convertArrayToJsonNode(List<?> list, Schema elementSchema) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (Object item : list) {
            JsonNode itemNode = convertValueToJsonNode(item, elementSchema);
            arrayNode.add(itemNode);
        }

        return arrayNode;
    }

    private JsonNode convertMapToJsonNode(Map<?, ?> map, Schema valueSchema) {
        ObjectNode objectNode = objectMapper.createObjectNode();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            JsonNode valueNode = convertValueToJsonNode(entry.getValue(), valueSchema);
            objectNode.set(key, valueNode);
        }

        return objectNode;
    }
}