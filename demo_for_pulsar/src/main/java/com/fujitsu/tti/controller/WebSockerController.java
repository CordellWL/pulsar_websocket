package com.fujitsu.tti.controller;

import com.fujitsu.tti.MQServer.Pulsar;
import io.github.majusko.pulsar.annotation.PulsarConsumer;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cordell
 * @date 2021/6/10
 */
@ServerEndpoint(path = "/ws/{arg}",port = "8999")
@Slf4j
public class WebSockerController {

        @Autowired
        Pulsar pulsar;


        private static final Map<String,Session> CONNECTION =new ConcurrentHashMap<>();

        @PulsarConsumer(topic="example-string-topic", clazz=String.class)
        public void send(String message){
            Session session = CONNECTION.get("1");
            System.out.println("开始发");
            session.sendText(message);
        }

        @BeforeHandshake
        public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
            session.setSubprotocols("stomp");
//            if (!"ok".equals(req)){
//                System.out.println("Authentication failed!");
//                session.close();
//            }
            System.out.println("Ok");
            CONNECTION.put("1",session);
            //todo创建一个新的监听
        }
        @OnOpen
        public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
            System.out.println("new connection");
//            System.out.println(req);

        }
        @OnClose
        public void onClose(Session session) throws IOException {
            //todo 关闭订阅
            System.out.println("one connection closed");
        }
        @OnError
        public void onError(Session session, Throwable throwable) {

            throwable.printStackTrace();
        }
        @OnMessage
        public void onMessage(Session session, String message) throws InterruptedException {
            //todo 创建pulsar订阅
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
