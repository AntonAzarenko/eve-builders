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
        List<Integer> groupIdsByUsername1 = allianceAuthRepository.findGroupIdsByUsername(userName);
        if (groupIdsByUsername1 != null  && groupIdsByUsername1.size() > 0) {
            return groupIdsByUsername1;
        }
        return allianceAuthRepository.findGroupIdsByUsername(userName.replace(" ", "_"));
    }
}
