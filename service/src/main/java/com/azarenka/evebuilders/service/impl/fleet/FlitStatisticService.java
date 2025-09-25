package com.azarenka.evebuilders.service.impl.fleet;

import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;
import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;
import com.azarenka.evebuilders.repository.auth.AllianceAuthRepository;
import com.azarenka.evebuilders.service.api.IFlitStatisticService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FlitStatisticService implements IFlitStatisticService {

    private final AllianceAuthRepository flightsRepository;

    public FlitStatisticService(AllianceAuthRepository flightsRepository) {
        this.flightsRepository = flightsRepository;
    }

    @Override
    public List<UserFleetStat> buildLeaderboard(FleetMetric metric, LocalDate from, LocalDate to) {
        List<UserFlightsInfo> raw = flightsRepository.findUserFlights(from, to);
        raw.sort(Comparator
            .comparingLong(UserFlightsInfo::getAppearancesCount).reversed()
            .thenComparing(UserFlightsInfo::getUsername, String.CASE_INSENSITIVE_ORDER));

        // 3) ранжируем (DENSE_RANK по appearancesCount)
        List<UserFleetStat> out = new ArrayList<>(raw.size());
        long prevValue = Long.MIN_VALUE;
        int rank = 0;

        for (int i = 0; i < raw.size(); i++) {
            UserFlightsInfo u = raw.get(i);
            long value = u.getAppearancesCount();

            if (value != prevValue) {
                rank = rank == 0 ? 1 : rank + 1; // dense-rank
                prevValue = value;
            }

            out.add(new UserFleetStat(
                u.getUsername(),
                u.getCharacterId(),
                rank,                       // место
                value,                      // значение (сколько КТА)
                metric,                     // метрика (KTA_ALL/KTA_MONTH)
                u.isTeamspeakConnected()    // active: можем подсвечивать, если подключен TS
            ));
        }
        return out;
    }
}
