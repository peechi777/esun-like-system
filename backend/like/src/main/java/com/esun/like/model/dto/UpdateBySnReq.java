package com.esun.like.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateBySnReq {
    private Long sn;

    @NotBlank
    private String account;

    @NotNull
    @Positive
    private Integer orderName;
}
