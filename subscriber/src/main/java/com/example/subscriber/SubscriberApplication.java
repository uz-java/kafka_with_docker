package com.example.subscriber;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;

@SpringBootApplication
@EnableKafka
public class SubscriberApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriberApplication.class, args);
    }
}

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
class Transaction {
    private Long id;
    private String pan;
    private BigDecimal amount;
    private Timestamp createdAt;
}

@Component
class TransactionListener {
    @KafkaListener(topics = Constants.TOPIC,groupId = Constants.GROUP_ID)
    public void listen(Transaction transaction){
        System.out.println("Received Message : "+transaction);
    }
}

class Constants {
    public static final String TOPIC="pdp-topic";
    public static final String GROUP_ID="group-id";
}