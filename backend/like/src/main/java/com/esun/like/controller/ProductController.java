package com.esun.like.controller;

import com.esun.like.model.dto.AddProductReq;
import com.esun.like.model.dto.ProductDto;
import com.esun.like.service.ProductService;
import com.esun.like.repository.ProductRepository.DeleteStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @DeleteMapping("/{productNo}")
    public ResponseEntity<?> delete(@PathVariable long productNo) {
        DeleteStatus status = service.delete(productNo);
        return switch (status) {
            case DELETED -> ResponseEntity.noContent().build(); // 204
            case IN_USE  -> ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("message", "此商品仍被喜好清單引用，請先刪除清單項目"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("message", "查無此商品"));
        };
    }
}
