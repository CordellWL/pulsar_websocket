package com.fujitsu.tti.Pojo;


import org.yeauty.pojo.Session;

/**
 * @author cordell
 * @date 2021/6/13
 */
public class MyConsumer {
    private String topic;
    private Session session;

    public MyConsumer(String topic, Session session) {
        this.topic = topic;
        this.session = session;
    }

    public String getTopic() {
        return topic;
    }

    public Session getSession() {
        return session;
    }
}
