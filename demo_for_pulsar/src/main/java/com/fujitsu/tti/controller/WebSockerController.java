package com.fujitsu.tti.controller;

import com.fujitsu.tti.MQServer.ConsumerClient;
import com.fujitsu.tti.Pojo.MyConsumer;
import io.github.majusko.pulsar.annotation.PulsarConsumer;
import io.github.majusko.pulsar.collector.ConsumerCollector;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cordell
 * @date 2021/6/10
 */
@ServerEndpoint(path = "/ws/{arg}",port = "8999")
@Slf4j
public class WebSockerController {

        @Autowired
        ConsumerCollector consumerCollector;

        @Autowired
        ConsumerClient client;

        public static final Map<Session,Consumer<?>> CONSUMERS =new ConcurrentHashMap<>();

        private static final Map<String,Session> CONNECTION =new ConcurrentHashMap<>();

        @BeforeHandshake
        public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
            session.setSubprotocols("stomp");
            System.out.println("Ok");
            CONNECTION.put("1",session);

            //todo创建一个新的监听
        }
        @OnOpen
        public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
            System.out.println("new connection");
//            Object subprotocols = session.getAttribute("subprotocols");
            Consumer<?> demo=null;
            if(arg.equals("demo2")){
            MyConsumer myConsumer=new MyConsumer("example-string-topic",session);
                 demo = client.subscribe("demo", myConsumer);
                CONSUMERS.put(session,demo);

            }else{
                MyConsumer myConsumer=new MyConsumer("example-string-topic"+"1",session);
                demo = client.subscribe("demo", myConsumer);
                CONSUMERS.put(session,demo);
            }
            CONSUMERS.put(session,demo);

        }
        @OnClose
        public void onClose(Session session) throws IOException {
            System.out.println("one connection closed");
            Consumer<?> consumer = CONSUMERS.get(session);
            consumer.close();
        }
        @OnError
        public void onError(Session session, Throwable throwable) {
            throwable.printStackTrace();
            session.close();
        }
        @OnMessage
        public void onMessage(Session session, String message) throws InterruptedException {
            System.out.println(message);
            session.sendText("Hello Netty!");
        }
        @OnBinary
        public void onBinary(Session session, byte[] bytes) {
            for (byte b : bytes) {
                System.out.println(b);
            }
            session.sendBinary(bytes);
        }
        @OnEvent
        public void onEvent(Session session, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                switch (idleStateEvent.state()) {
                    case READER_IDLE:
                        System.out.println("read idle");
                        break;
                    case WRITER_IDLE:
                        System.out.println("write idle");
                        break;
                    case ALL_IDLE:
                        System.out.println("all idle");
                        break;
                    default:
                        break;
                }
            }
        }


}
