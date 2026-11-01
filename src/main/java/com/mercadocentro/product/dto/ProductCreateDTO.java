package com.mercadocentro.product.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductCreateDTO(@NotBlank String name, String description, @NotNull @Positive BigDecimal price, @NotNull @PositiveOrZero Integer stock, String imageUrl, BigDecimal cost) {}