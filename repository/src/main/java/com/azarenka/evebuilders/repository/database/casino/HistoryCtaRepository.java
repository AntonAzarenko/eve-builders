package com.azarenka.evebuilders.repository.database.casino;

import com.azarenka.evebuilders.domain.casino.HistoryCta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryCtaRepository extends JpaRepository<HistoryCta, String> {

    List<HistoryCta> findHistoryCtaByCharacterId(Integer characterId);

    HistoryCta findHistoryCtaByCharacterIdAndEventDate(Integer characterId, LocalDateTime dateTime);
}
