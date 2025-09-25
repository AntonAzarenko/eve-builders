package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.repository.auth.AllianceAuthRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AllianceAuthService {

    @Autowired
    private AllianceAuthRepository allianceAuthRepository;

    public List<Integer> findGroupIdsByUsername(String userName) {
        return allianceAuthRepository.findGroupIdsByUsername(userName);
    }
}
