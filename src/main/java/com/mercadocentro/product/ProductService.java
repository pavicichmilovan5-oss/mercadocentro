package com.mercadocentro.product;

import com.mercadocentro.product.dto.ProductAdminDTO;
import com.mercadocentro.product.dto.ProductCatalogDTO;
import com.mercadocentro.product.dto.ProductCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductCatalogDTO> getCatalog() {
        return productRepository.findAll().stream()
            .map(p -> new ProductCatalogDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getImageUrl(),
                p.getStock() > 0
            )).toList();
    }

    @Transactional
    public ProductAdminDTO create(ProductCreateDTO dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        product.setImageUrl(dto.imageUrl());

        Product saved = productRepository.save(product);

       return new ProductAdminDTO(saved.getId(), saved.getName(), saved.getDescription(), saved.getPrice(), saved.getCost(), saved.getStock(), saved.getImageUrl(), saved.isActive());
    }
}