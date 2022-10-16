package com.example.subscriber;

import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

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

@Configuration
class KafkaConsumerConfig{
    @Value("${kafka.server}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String,Transaction> consumerFactory(){
        Map<String,Object> config=new HashMap<>();
        JsonDeserializer<Transaction> deserializer=new JsonDeserializer<>(Transaction.class);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"group_id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,deserializer);
        return new DefaultKafkaConsumerFactory<>(config,new StringDeserializer(),deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,Transaction> transactionConcurrentKafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,Transaction> factory=new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public StringJsonMessageConverter jsonMessageConverter(){
        return new StringJsonMessageConverter();
    }
}