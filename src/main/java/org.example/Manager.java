package org.example;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Manager {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ManagerDB managerDB = context.getBean("managerDBBean", ManagerDB.class);
        Producer producer = context.getBean("producerBean", Producer.class);
        Consumer consumer = context.getBean("consumerBean", Consumer.class);
    }
}
