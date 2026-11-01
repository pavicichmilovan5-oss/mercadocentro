package com.mercadocentro.product;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private BigDecimal price;

  private BigDecimal cost; 

  private String imageUrl;

  private int stock;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;
}