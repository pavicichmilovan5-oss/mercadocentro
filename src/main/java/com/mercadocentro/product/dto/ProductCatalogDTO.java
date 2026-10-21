package com.mercadocentro.product.dto;
import java.math.BigDecimal;
public record ProductCatalogDTO(Long id, String name, BigDecimal price, String imageUrl, Boolean available) {}