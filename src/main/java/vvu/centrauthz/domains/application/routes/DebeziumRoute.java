package vvu.centrauthz.domains.application.routes;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Struct;
import vvu.centrauthz.utilities.JsonTools;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class DebeziumRoute extends RouteBuilder {

    private String extractTable(Exchange exchange) {
        //
        Object metadata = exchange.getIn().getHeader("CamelDebeziumIdentifier");

        try {
            return Arrays.stream(metadata.toString().split("\\.")).toList().getLast();
        } catch (Exception e) {
            log.error("Failed to extract key from message", e);
        }

        return null;
    }

    private String extractKey(Exchange exchange) {

        try {

            exchange.getIn().getHeaders().forEach((key, value) -> log.info("Header: {} = {}", key, value));

            var tableName = extractTable(exchange);
            var messageKey = exchange.getIn().getHeader("CamelDebeziumKey");

            if (messageKey instanceof Struct keyStruct) {
                log.info("keyStruct {}", keyStruct);

                var keyName = Objects.equals(tableName, "application") ? "application_key" : "id";
                Object primaryKeyId = keyStruct.get(keyName);

                Optional.ofNullable(primaryKeyId).ifPresent(id ->
                        exchange.getIn().setHeader("id", id));

                return Objects.nonNull(primaryKeyId) ? primaryKeyId.toString() : null;
            }
        } catch (Exception e) {
            log.error("Failed to extract key from message", e);
        }

        return null;
    }

    @Override
    public void configure() throws Exception {

        from("debezium-postgres:local-application")
                .routeId("application-cdc-route")
                .log("Received CDC event: ${body}")
                .log("Operation: ${header.CamelDebeziumOperation}")
                .log("Table: ${header.CamelDebeziumSourceTable}")
                .choice()
                .when(header("CamelDebeziumOperation").isEqualTo("c"))
                .log("Processing CREATE operation")
                .convertBodyTo(JsonNode.class)
                .process(exchange -> {
                    JsonNode jsonBody = exchange.getIn().getBody(JsonNode.class);
                    log.info("Create operation - JSON: {}", jsonBody);
                    // You can access specific fields like: jsonBody.get("fieldName")
                })
                .when(header("CamelDebeziumOperation").isEqualTo("u"))
                .log("Processing UPDATE operation")
                .convertBodyTo(JsonNode.class)
                .process(exchange -> {
                    JsonNode jsonBody = exchange.getIn().getBody(JsonNode.class);
                    log.info("Update operation - JSON: {}", jsonBody);
                    // Access before/after values: jsonBody.get("before"), jsonBody.get("after")
                })
                .when(header("CamelDebeziumOperation").isEqualTo("d"))
                .log("Processing DELETE operation")
                .convertBodyTo(JsonNode.class)
                .process(exchange -> {
                    JsonNode jsonBody = exchange.getIn().getBody(JsonNode.class);
                    var key = extractKey(exchange);

                    log.info("Delete operation Key {} - JSON: {}", key, jsonBody);

                    if (jsonBody == null) {
                        return;
                    }

                    // Extract key information for deletion
                    JsonNode beforeData = jsonBody.get("before");
                    if (beforeData != null) {
                        log.info("Deleted record data: {}", beforeData);
                    }
                })
                .log("Deleted record ID: ${header.id}")
                .otherwise()
                .log("Unknown operation: ${header.CamelDebeziumOperation}");
    }
}