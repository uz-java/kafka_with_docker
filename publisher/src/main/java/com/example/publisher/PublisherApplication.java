package com.example.publisher;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

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
class KafkaService {
    public final String TOPIC = "pdp-topic";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(Transaction message) {
        kafkaTemplate.send(TOPIC, message);
    }

}