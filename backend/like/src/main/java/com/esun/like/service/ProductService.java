package com.esun.like.service;

import com.esun.like.model.dto.AddProductReq;
import com.esun.like.model.dto.ProductDto;
import com.esun.like.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    @Transactional
    public long create(AddProductReq req) {
        return repo.create(req);
    }

    public List<ProductDto> list() {
        return repo.list();
    }
    public ProductRepository.DeleteStatus delete(long productNo) {
        return repo.delete(productNo);
    }
}
