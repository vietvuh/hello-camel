package vvu.centrauthz.domains.helloworld.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.model.rest.RestBindingMode;
import vvu.centrauthz.domains.helloworld.models.Ping;
import org.apache.camel.builder.RouteBuilder;
import vvu.centrauthz.utilities.ErrorTools;
import vvu.centrauthz.utilities.JsonTools;

@ApplicationScoped
@Slf4j
public class RestRoute  extends RouteBuilder {

    private void handleValidationException(Exchange exchange) {
        ValidationException validationException =
                exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ValidationException.class);

        var error = ErrorTools.parseValidationError(validationException.getMessage());

        exchange.getIn().setBody(JsonTools.toString(error));
    }

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
        // Global exception handling
        onException(IllegalArgumentException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                .setBody(constant("Bad Request: Invalid argument"));
        // BeanValidationException
        onException(jakarta.validation.ValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                .setBody(constant("Bad Request: Invalid argument"));

        onException(org.apache.camel.ValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                .process(this::handleValidationException);

        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .log("Exception: ${exception}")
                .setBody(constant("Internal Server Error"));


        restConfiguration()
                .component("platform-http")
                .bindingMode(RestBindingMode.json)
                .jsonDataFormat("jackson")
                .dataFormatProperty("prettyPrint", "true")
                .dataFormatProperty("include", "NON_NULL");

        rest("/api/v0")
            .get("ping")
            .to("direct:ping")
            .post("ping")
            .type(Ping.class)
            .consumes(MediaType.APPLICATION_JSON)
            .produces(MediaType.APPLICATION_JSON)
            .to("direct:pingPost");

        from("direct:ping")
            .routeId("ping-route")
            .process(exchange -> {
                var ping = Ping.builder().message("pong").timestamp(System.currentTimeMillis()).build();
                exchange.getIn().setBody(ping);
            })
            .setHeader("Content-Type", constant("application/json"));

        from("direct:pingPost")
            .routeId("ping-post-route")
                .to("bean-validator://ping")
                .process(exchange -> {
                    Ping ping = exchange.getIn().getBody(Ping.class);
                    // echo back the valid ping
                    exchange.getIn().setBody(ping);
                });
    }
}
