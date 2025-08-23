package com.esun.like.controller;

import com.esun.like.model.dto.SaveUserReq;
import com.esun.like.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping(value = "/api/users", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public Map<String, Object> upsert(@Valid @RequestBody SaveUserReq req) {
        service.upsert(req);
        return Map.of("ok", true);
    }
}
