package com.neutroware.ebaysyncserver.product;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getProductsByUserId(String userId) {
        return productRepository.findByUserId(userId).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }
}
