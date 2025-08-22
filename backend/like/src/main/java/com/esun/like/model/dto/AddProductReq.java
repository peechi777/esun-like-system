package com.esun.like.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddProductReq {

    @NotBlank
    private String productName;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false) // > 0
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0.0000", inclusive = true) // 允許 0
    private BigDecimal feeRate;
}
