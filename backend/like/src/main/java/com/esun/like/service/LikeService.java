package com.esun.like.service;

import com.esun.like.model.dto.AddOrUpdateLikeReq;
import com.esun.like.model.dto.UpdateBySnReq;
import com.esun.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository repo;

    @Transactional
    public void addOrMerge(AddOrUpdateLikeReq req) {
        repo.addOrMerge(req);
    }

    @Transactional
    public void updateBySn(UpdateBySnReq req) {
        repo.updateBySn(req);
    }

    @Transactional
    public void deleteBySn(Long sn) {
        repo.deleteBySn(sn);
    }
}
