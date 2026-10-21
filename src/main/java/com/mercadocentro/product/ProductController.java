package com.mercadocentro.product;

import com.mercadocentro.product.dto.ProductAdminDTO;
import com.mercadocentro.product.dto.ProductCatalogDTO;
import com.mercadocentro.product.dto.ProductCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;                                                        

    @GetMapping
    public List<ProductCatalogDTO> catalog() {
        return productService.getCatalog();
    }

    @PostMapping
    public ResponseEntity<ProductAdminDTO> create(@Valid @RequestBody ProductCreateDTO dto) {
        ProductAdminDTO created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}