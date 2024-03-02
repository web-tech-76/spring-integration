package intgration1.app;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Intgration1Application {

    public static void main(String[] args) {
        SpringApplication.run(Intgration1Application.class, args);
    }

    @Bean
    MessageChannel messageChannelAtoB() {
        return MessageChannels.direct().getObject();
    }


    static String getPayLoad() {
        return Math.random() > .5 ? "hello world  @ rate -> " + Instant.now() :
                "bonjour le monde  @ rate -> " + Instant.now();
    }

    @Component
    static class MyMessageSource implements MessageSource<String> {

        @Override
        public Message<String> receive() {
            return MessageBuilder.withPayload(getPayLoad()).build();
        }
    }


    // no need when provided the MessageSource
    @Bean
    ApplicationRunner applicationRunner(MyMessageSource source, IntegrationFlowContext ifc) {
        return args -> {

//            IntStream.range(0, 10).forEach(num -> {
            //messageChannelAtoB().send(MessageBuilder.withPayload(getPayLoad()).build())
            var flow1 = buildIntegrationFlow(source, 2000, "bonjour");
            var flow2 = buildIntegrationFlow(source, 2000, "hello");

            Set.of(flow1, flow2)
                    .forEach(flow -> ifc.registration(flow).register().start());
//                    }
//            );
        };
    }


    static IntegrationFlow buildIntegrationFlow(MyMessageSource myMessageSource, int seconds, String filterText) {
        return IntegrationFlow
//                .from((MessageSource<String>) () -> MessageBuilder
//                                .withPayload(getPayLoad())
//                                .build(),
//                        (SourcePollingChannelAdapterSpec poller) -> poller.poller(
//                                pollerMeta -> pollerMeta.fixedRate(100)))

                .from(myMessageSource, p -> p.poller(pf ->
                        pf.fixedRate(seconds, TimeUnit.SECONDS.toSeconds(0)))
                )
                .filter(String.class, (source) -> source.contains(filterText))
                .transform((GenericTransformer<String, String>) String::toUpperCase)

//                .handle((GenericHandler<String>) (payload, headers) -> payload.contains("bonjour") ? payload : null)
//                .handle((GenericHandler<String>) (payload, headers) -> payload.toUpperCase())
                .handle((GenericHandler<String>) (payload, headers) -> {
                    System.out.println("payload =  for text [ " + filterText + " ] -> " + payload);
                    return null;
                })
//                .channel(messageChannelAtoB())
                .get();
    }



    /*@Bean
    IntegrationFlow flowReceive() {
        return IntegrationFlow
                .from(messageChannelAtoB())
                .handle((GenericHandler<String>) (payload, headers) -> {
                    System.out.println("payload = " + payload);
                    return null;
                })
                .get();
    }*/

}