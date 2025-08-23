package vvu.centrauthz.domains.helloworld.routes;

import org.apache.camel.builder.RouteBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class HelloWorldRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:helloWorld?period=30000")
                .routeId("hello-world-timer")
                .log("Hello World! Current time: ${date:now:yyyy-MM-dd HH:mm:ss}")
                .process(exchange -> {
                    String message = "Hello World from Camel Quarkus! Timestamp: " +
                            java.time.LocalDateTime.now().toString();
                    log.info(message);
                    exchange.getIn().setBody(message);
                });
    }
}
