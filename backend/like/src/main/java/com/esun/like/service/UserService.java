package com.esun.like.service;

import com.esun.like.model.dto.SaveUserReq;
import com.esun.like.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    @Transactional
    public void upsert(SaveUserReq req) {
        repo.upsert(req);
    }
}
