package com.esun.like.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddOrUpdateLikeReq {
    @NotBlank
    private String userId;

    
    @NotNull
    private Long productNo;

    @NotBlank
    private String account;

    @NotNull
    @Positive
    private Integer orderName;
}
