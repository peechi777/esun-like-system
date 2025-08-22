package com.esun.like.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UpdateBySnReq {
    @NotNull
    private Long sn;

    @NotBlank
    private String account;

    @NotNull
    @Positive
    private Integer order name;
}
