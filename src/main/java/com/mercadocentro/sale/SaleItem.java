package com.mercadocentro.sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.mercadocentro.product.Product;

@Entity
@Table(name = "sale_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sale_id", nullable = false)
  private Sale sale;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @PrePersist
  @PreUpdate
  void calculateSubtotal() {
    if (price == null) {
        throw new IllegalStateException("SaleItem requiere price");
    }
    if (quantity <= 0) {
        throw new IllegalStateException("SaleItem requiere quantity > 0");
    }
    this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
  }
}