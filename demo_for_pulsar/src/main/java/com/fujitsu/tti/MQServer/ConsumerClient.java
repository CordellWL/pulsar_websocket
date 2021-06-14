package com.fujitsu.tti.MQServer;

import com.fujitsu.tti.Pojo.MyConsumer;
import io.github.majusko.pulsar.constant.Serialization;
import io.github.majusko.pulsar.error.FailedMessage;
import io.github.majusko.pulsar.error.exception.ConsumerInitException;
import io.github.majusko.pulsar.properties.ConsumerProperties;
import io.github.majusko.pulsar.utils.SchemaUtils;
import io.github.majusko.pulsar.utils.TopicUrlService;
import io.netty.buffer.ByteBuf;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;
import org.yeauty.pojo.Session;
import reactor.core.publisher.EmitterProcessor;

import java.util.concurrent.TimeUnit;

/**
 * @author cordell
 * @date 2021/6/13
 */
@Component
public class ConsumerClient {
    @Autowired
    private PulsarClient pulsarClient;
    @Autowired
    private  ConsumerProperties consumerProperties;
    @Autowired
    private  TopicUrlService topicUrlService;

    private final EmitterProcessor<FailedMessage> exceptionEmitter = EmitterProcessor.create();

    private StringValueResolver stringValueResolver;

    public Consumer<?> subscribe(String name, MyConsumer holder) {
        //todo 基于链接信息进行过滤
        try {
            final ConsumerBuilder<?> consumerBuilder = pulsarClient
                    .newConsumer(SchemaUtils.getSchema(Serialization.JSON,
                            String.class))
                    .consumerName("consumer-" + name)
                    .subscriptionName("subscription-" + name)
                    .topic(topicUrlService
                            .buildTopicUrl(holder.getTopic()))
                    .subscriptionType(SubscriptionType.Exclusive)
                    .messageListener(
                            (consumer, msg) -> {
                        try {
                            Session session = holder.getSession();
                            System.out.println(session+"收到"+msg.getValue());
                            session.sendText((String) msg.getValue());
                            consumer.acknowledge(msg);
                        } catch (Exception e) {
                            consumer.negativeAcknowledge(msg);
                            exceptionEmitter.onNext(new FailedMessage(e, consumer, msg));
                        }
                    });

            if (consumerProperties.getAckTimeoutMs() > 0) {
                consumerBuilder.ackTimeout(consumerProperties.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
            }
            return consumerBuilder.subscribe();
        } catch (PulsarClientException e) {
            throw new ConsumerInitException("Failed to init consumer.", e);
        }
    }

}
