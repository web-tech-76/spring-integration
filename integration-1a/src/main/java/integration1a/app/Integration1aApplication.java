package integration1a.app;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.IntStream;

@IntegrationComponentScan
@SpringBootApplication
public class Integration1aApplication {

    public static void main(String[] args) {
        SpringApplication.run(Integration1aApplication.class, args);
    }

    @Bean
    MessageChannel greetings() {
        return MessageChannels.direct().getObject();
    }


    static String payLoad() {
        return Math.random() > .5 ?
                "hello world @" + Instant.now()
                : "hola mundo @" + Instant.now();
    }

    @Bean
    IntegrationFlow buildIntegrationFlow() {
        return IntegrationFlow
//                .from((MessageSource<String>) () -> MessageBuilder.withPayload(payLoad()).build(),
//                        p -> p.poller(pf -> pf.fixedRate(2000, 2001)))
                .from(greetings())
                .filter(String.class, source -> source.contains("hola"))
                .transform((GenericTransformer<String, String>) str -> str.toUpperCase())
                .handle((payload, headers) -> {
                            System.out.println("payload = " + payload);
                            return null;
                        }
                )
                .get();

    }


}


@Component
class MessagingClient implements ApplicationRunner {

    private final GreetingsClient greetingsClient;

    public MessagingClient(GreetingsClient greetingsClient) {
        this.greetingsClient = greetingsClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IntStream.range(0, 10).forEach(num -> {
//            new Integration1aApplication().payLoad();
            greetingsClient.greet(Integration1aApplication.payLoad());
        });
    }
}


@MessagingGateway(defaultRequestChannel = "greetings")
interface GreetingsClient {

    void greet(String text);

}
