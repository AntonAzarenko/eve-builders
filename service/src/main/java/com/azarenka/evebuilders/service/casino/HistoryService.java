package com.azarenka.evebuilders.service.casino;

import com.azarenka.evebuilders.domain.casino.HistoryCta;
import com.azarenka.evebuilders.repository.database.casino.HistoryCtaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryCtaRepository historyCtaRepository;

    public List<HistoryCta> getHistoryCta(int id) {
        return historyCtaRepository.findHistoryCtaByCharacterId(id);
    }
}
