package com.example.publisher;

import lombok.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableKafka
public class PublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublisherApplication.class, args);
    }

}

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String pan;
    @Column(nullable = false)
    @Formula("amount > 0.0")
    private BigDecimal amount;
    @CreatedDate
    @CreationTimestamp
    @Column(columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class TransferCreateVO {
    private String pan;
    private BigDecimal amount;
}

interface TransactionRepository extends JpaRepository<Transaction, Long> {

}

@Service
@RequiredArgsConstructor
class TransactionService {
    private final TransactionRepository repository;
    private final KafkaService kafkaService;

    public Transaction create(TransferCreateVO vo) {
        Transaction transaction = Transaction.builder()
                .pan(vo.getPan())
                .amount(vo.getAmount())
                .build();
        transaction = repository.save(transaction);
        kafkaService.send(transaction);
        return transaction;
    }
}

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
class PublisherController {
    private final TransactionService service;
}

@Service
@RequiredArgsConstructor
class KafkaService {
    public final String TOPIC = "pdp-topic";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(Transaction message) {
        kafkaTemplate.send(TOPIC, message);
    }
}

@Configuration
class KafkaProducerConfig {
    @Value("${kafka.server}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}