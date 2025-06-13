package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    @Setter
    private boolean active = true;

    @Column(nullable = false)
    private int displayOrder;

    @Builder
    public Category(String name, String description, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void update(String name, String description, Integer displayOrder) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.description = description;
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }
}
