package com.mercadocentro.sale;

import com.mercadocentro.product.Product;
import com.mercadocentro.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaleService {
  private final ProductRepository productRepo;
  private final SaleRepository saleRepo;
  private final InvoiceSequenceRepository seqRepo;

  @Transactional
  public Sale createSale(Map<Long, Integer> items, String idempotencyKey) {
    if (items == null || items.isEmpty()) {
        throw new IllegalArgumentException("Carrito vacío");
    }

    if (idempotencyKey != null) {
        var existing = saleRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return existing.get();
    }

    InvoiceSequence seq = seqRepo.findByIdForUpdate(1L)
        .orElse(new InvoiceSequence(1L, 0L));
    seq.setLastNumber(seq.getLastNumber() + 1);
    seqRepo.save(seq);

    String invoiceNumber = String.format("FAC-%d-%06d", LocalDate.now().getYear(), seq.getLastNumber());

    Sale sale = Sale.builder()
      .status(SaleStatus.PENDING)
      .idempotencyKey(idempotencyKey)
      .invoiceNumber(invoiceNumber)
      .total(BigDecimal.ZERO)
      .build();

    BigDecimal total = BigDecimal.ZERO;
    List<SaleItem> saleItems = new ArrayList<>();

    for (var e : items.entrySet()) {
      Long productId = e.getKey();
      int qty = e.getValue();
      if (qty <= 0) throw new IllegalArgumentException("Cantidad inválida para producto " + productId);

      Product p = productRepo.findByIdForUpdate(productId)
        .orElseThrow(() -> new EntityNotFoundException("Producto " + productId + " no encontrado"));
      
      if (p.getStock() < qty) {
          throw new InsufficientStockException(p.getName());
      }

      p.setStock(p.getStock() - qty);

      SaleItem item = SaleItem.builder()
        .product(p)
        .sale(sale)
        .quantity(qty)
        .price(p.getPrice())
        .build();
      
      saleItems.add(item);
      total = total.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
    }

    sale.setTotal(total);
    sale.setItems(saleItems);

    try {
        return saleRepo.save(sale);
    } catch (DataIntegrityViolationException ex) {
        if (idempotencyKey != null) {
            return saleRepo.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> ex);
        }
        throw ex;
    }
  }
}