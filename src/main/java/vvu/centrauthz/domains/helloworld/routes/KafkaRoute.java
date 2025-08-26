package vvu.centrauthz.domains.helloworld.routes;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
@Slf4j
public class KafkaRoute extends RouteBuilder  {
    /**
     * <b>Called on initialization to build the routes using the fluent builder syntax.</b>
     * <p/>
     * This is a central method for RouteBuilder implementations to implement the routes using the Java fluent builder
     * syntax.
     *
     * @throws Exception can be thrown during configuration
     */
    @Override
    public void configure() throws Exception {
        from("direct:produce-cdc-events")
            .routeId("produce-cdc-events-route")
            .log("Producing CDC event to Kafka: ${body}")
            .marshal().json()
            .to("kafka:topic.cdc.events.applications");

        from("kafka:topic.cdc.events.applications")
            .routeId("consume-cdc-applications-route")
            .log("Consumed CDC user event from Kafka: ${body}");

    }
}
