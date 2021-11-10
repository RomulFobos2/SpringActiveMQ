package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Producer implements Runnable {
    private ActiveMQConnectionFactory connectionFactory;
    private Session sessionProducer;
    private Connection connectionProducer;
    private Destination destination;
    private String queue;
    private MessageProducer messageProducer;
    private int persistent; //1 or 2

    public Producer(ActiveMQConnectionFactory connectionFactory, int sessionCode, String queue, int persistent) throws JMSException {
        this.connectionFactory = connectionFactory;
        this.connectionProducer = this.connectionFactory.createConnection();
        connectionProducer.start();
        if (sessionCode > 0) {
            this.sessionProducer = this.connectionProducer.createSession(false, sessionCode);
        } else {
            this.sessionProducer = this.connectionProducer.createSession(true, sessionCode);
        }
        this.destination = this.sessionProducer.createQueue(queue);
        this.queue = queue;
        this.persistent = persistent;

        Thread thread = new Thread(this, "producer");
        thread.start();

    }

    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Session getSessionProducer() {
        return sessionProducer;
    }

    public void setSessionProducer(Session sessionProducer) {
        this.sessionProducer = sessionProducer;
    }

    public Connection getConnectionProducer() {
        return connectionProducer;
    }

    public void setConnectionProducer(Connection connectionProducer) {
        this.connectionProducer = connectionProducer;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public MessageProducer getMessageProducer() {
        return messageProducer;
    }

    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @Override
    public void run() {
        try {
            messageProducer = sessionProducer.createProducer(destination);
            messageProducer.setDeliveryMode(persistent);
            while (true){
                String randomBodyMessage = String.valueOf(UUID.randomUUID()).replace("-", "").substring(0, 16);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String themeMessage = dateFormat.format(new Date());
                TextMessage message = sessionProducer.createTextMessage(randomBodyMessage);
                message.setStringProperty("theme", themeMessage);
                messageProducer.send(message);
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (JMSException exception) {
            exception.printStackTrace();
        }
    }
}
