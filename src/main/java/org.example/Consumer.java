package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Consumer implements Runnable {
    private ActiveMQConnectionFactory connectionFactory;
    private Session sessionConsumer;
    private Connection connectionConsumer;
    private Destination destination;
    private String queue;
    private MessageConsumer messageConsumer;

    public Consumer(ActiveMQConnectionFactory connectionFactory, int sessionCode, String queue) throws JMSException {
        this.connectionFactory = connectionFactory;
        this.connectionConsumer = this.connectionFactory.createConnection();
        connectionConsumer.start();
        if (sessionCode > 0) {
            this.sessionConsumer = this.connectionConsumer.createSession(false, sessionCode);
        } else {
            this.sessionConsumer = this.connectionConsumer.createSession(true, sessionCode);
        }
        this.destination = this.sessionConsumer.createQueue(queue);
        this.queue = queue;

        Thread thread = new Thread(this, "consumer");
        thread.start();
    }

    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Session getSessionConsumer() {
        return sessionConsumer;
    }

    public void setSessionConsumer(Session sessionConsumer) {
        this.sessionConsumer = sessionConsumer;
    }

    public Connection getConnectionConsumer() {
        return connectionConsumer;
    }

    public void setConnectionConsumer(Connection connectionConsumer) {
        this.connectionConsumer = connectionConsumer;
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

    public MessageConsumer getConsumer() {
        return messageConsumer;
    }

    public void setConsumer(MessageConsumer consumer) {
        this.messageConsumer = consumer;
    }

    @Override
    public void run() {
        try {
            messageConsumer = sessionConsumer.createConsumer(destination);
            messageConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        java.sql.Connection connectionToDB = ManagerDB.getCurrentConnection();
                        Statement statement = connectionToDB.createStatement();
                        String sqlCommand;

                        if (!ManagerDB.tableExist("properties")) {
                            sqlCommand = "Create TABLE properties(property_id INT PRIMARY KEY AUTO_INCREMENT, property_message VARCHAR(100));";
                            statement.executeUpdate(sqlCommand);
                        }
                        if (!ManagerDB.tableExist("messages")) {
                            sqlCommand = "Create TABLE messages(message_id INT PRIMARY KEY AUTO_INCREMENT, body_message VARCHAR(100), property_id INT, FOREIGN KEY (property_id)  REFERENCES properties (property_id));";
                            statement.executeUpdate(sqlCommand);
                        }

                        sqlCommand = "INSERT properties (property_message) VALUE('" + textMessage.getStringProperty("theme") + "')";
                        statement.executeUpdate(sqlCommand, Statement.RETURN_GENERATED_KEYS);

                        ResultSet rs = statement.getGeneratedKeys();
                        rs.next();
                        sqlCommand = "INSERT messages (body_message, property_id) VALUES('" + textMessage.getText() + "', " + rs.getLong(1) + ")";
                        statement.executeUpdate(sqlCommand);


                    } catch (JMSException | SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            });
        } catch (JMSException exception) {
            exception.printStackTrace();
        }
    }
}
