package com.esun.like.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveUserReq {
    @NotBlank
    private String userId;

    @NotBlank
    private String userName;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String account;
}
