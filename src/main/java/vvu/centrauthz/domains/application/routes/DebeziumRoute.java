package vvu.centrauthz.domains.application.routes;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Struct;
import vvu.centrauthz.models.CdcEvent;
import vvu.centrauthz.models.CdcEventType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class DebeziumRoute extends RouteBuilder {
    private final String brokers;

    public DebeziumRoute(@ConfigProperty(name = "camel.component.kafka.brokers") String brokers) {
        this.brokers = brokers;
    }

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
            var tableName = extractTable(exchange);
            var messageKey = exchange.getIn().getHeader("CamelDebeziumKey");

            if (messageKey instanceof Struct keyStruct) {
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

    private CdcEventType extractHeader(Exchange exchange) {
        Object metadata = exchange.getIn().getHeader("CamelDebeziumOperation");
        return switch (metadata.toString()) {
            case "c" -> CdcEventType.CREATED;
            case "u" -> CdcEventType.UPDATED;
            case "d" -> CdcEventType.DELETED;
            default -> CdcEventType.UNKNOWN;
        };
    }

    private CdcEvent extract(Exchange exchange) {
        var kind =exchange.getIn().getHeader("CamelDebeziumIdentifier");
        var builder = CdcEvent.builder();
        builder.event(extractHeader(exchange));
        builder.kind(Objects.nonNull(kind) ? kind.toString() : "Unknown");
        builder.after(exchange.getIn().getBody(JsonNode.class));
        builder.key(extractKey(exchange));

        return builder.build();
    }

    private void process(Exchange exchange) {
        var e = extract(exchange);
        exchange.getIn().setBody( e.toJson());
    }

    @Override
    public void configure() throws Exception {

        from("debezium-postgres:local-application?additional-properties.bootstrap.servers=" + brokers)
                .routeId("application-cdc-route")
                .log("Received CDC event: ${body}")
                .log("Operation: ${header.CamelDebeziumOperation}")
                .log("Table: ${header.CamelDebeziumIdentifier}")
                .choice()
                    .when(header("CamelDebeziumOperation").in("c", "u", "d"))
                        .convertBodyTo(JsonNode.class)
                        .process(this::process)
                        .log("Event ${body}")
                        .to("direct:produce-cdc-events")
                    .otherwise()
                .log("Unknown operation: ${header.CamelDebeziumOperation}");
    }
}