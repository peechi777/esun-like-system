package com.esun.like.controller;

import com.esun.like.model.dto.AddOrUpdateLikeReq;
import com.esun.like.model.dto.UpdateBySnReq;
import com.esun.like.model.dto.ListLikesResponse;
import com.esun.like.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping(value = "/api/likes", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService service;

    @PostMapping
    public ResponseEntity<Void> addOrMerge(@Valid @RequestBody AddOrUpdateLikeReq req) {
        service.addOrMerge(req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{sn}")
    public ResponseEntity<Void> update(@PathVariable Long sn,
                                       @Valid @RequestBody UpdateBySnReq req) {
        req.setSn(sn);
        service.updateBySn(req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sn}")
    public ResponseEntity<Void> delete(@PathVariable Long sn) {
        service.deleteBySn(sn);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ListLikesResponse> list(@PathVariable String userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }
}
