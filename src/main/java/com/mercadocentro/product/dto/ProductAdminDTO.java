package com.mercadocentro.product.dto;
import java.math.BigDecimal;
public record ProductAdminDTO(Long id, String name, String description, BigDecimal price, BigDecimal cost, Integer stock, String imageUrl, boolean active) {}