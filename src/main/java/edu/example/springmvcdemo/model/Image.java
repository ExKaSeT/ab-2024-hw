package edu.example.springmvcdemo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "images")
@Data
public class Image {
    /**
     Object name in storage
     */
    @Id
    @Column(name = "link", length = 50, nullable = false)
    private String link;

    /**
     Original filename
     */
    @Column(name = "original_name", length = 100, nullable = false)
    private String originalName;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_username", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "originalImage", orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ImageProcessing> imageProcessing;
}