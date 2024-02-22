package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq")
    @SequenceGenerator(name = "message_seq", sequenceName = "message_seq", allocationSize = 1)
    private Long id;

    @Column(name = "content")
    private String content;
}
