package com.mercadocentro.sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sale {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(unique = true)
  private String idempotencyKey;

  @Column(unique = true)
  private String invoiceNumber;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private SaleStatus status = SaleStatus.PENDING;

  private BigDecimal total;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime paidAt;
  private String mpPaymentId;
  private String mpPreferenceId;

  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
  private List<SaleItem> items;
}