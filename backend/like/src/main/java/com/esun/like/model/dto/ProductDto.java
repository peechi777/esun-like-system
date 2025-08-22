package com.esun.like.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long productNo;        
    private String productName;    
    private BigDecimal price;      
    private BigDecimal feeRate;    
}
