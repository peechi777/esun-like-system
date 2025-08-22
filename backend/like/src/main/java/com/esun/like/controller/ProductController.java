package com.esun.like.controller;

import com.esun.like.model.dto.AddProductReq;
import com.esun.like.model.dto.ProductDto;
import com.esun.like.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping(value = "/api/products", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody AddProductReq req) {
        long no = service.create(req);
        return Map.of("productNo", no);
    }

    @GetMapping
    public List<ProductDto> list() {
        return service.list();
    }
}
