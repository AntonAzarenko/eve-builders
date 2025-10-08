package com.azarenka.evebuilders.service.impl.fleet;

import com.azarenka.evebuilders.domain.authdb.CtaFleetInfo;
import com.azarenka.evebuilders.domain.authdb.UserFlightsInfo;
import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.enums.FleetMetric;
import com.azarenka.evebuilders.repository.auth.AAFleetTrackerRepository;
import com.azarenka.evebuilders.repository.auth.AllianceAuthRepository;
import com.azarenka.evebuilders.service.api.IFlitStatisticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

@Service
public class FlitStatisticService implements IFlitStatisticService {

    private final AllianceAuthRepository flightsRepository;
    @Autowired
    private AAFleetTrackerRepository fleetTrackerRepository;

    public FlitStatisticService(AllianceAuthRepository flightsRepository) {
        this.flightsRepository = flightsRepository;
    }

    @Override
    public Set<UserFleetStat> buildLeaderboard(FleetMetric metric, LocalDate from, LocalDate to) {
        LocalDateTime dateTime = LocalDateTime.of(from.getYear(), from.getMonth(), from.getDayOfMonth(), 0, 0, 0);
        List<CtaFleetInfo> fleets = fleetTrackerRepository.findFleetsByDate(dateTime);
        Map<String, LongAdder> counts = new HashMap<>();
        Map<String, UserFlightsInfo> lastInfo = new HashMap<>();

        for (CtaFleetInfo fleet : fleets) {
            // 1) Собираем всех уникальных участников одного флота
            Set<String> usernamesInFleet = new HashSet<>();

            List<UserFlightsInfo> participants = fleetTrackerRepository.findUserFlights(fleet.getId());
            if (participants != null) {
                for (UserFlightsInfo u : participants) {
                    if (u == null || u.getUsername() == null) continue;
                    usernamesInFleet.add(u.getUsername());
                    // можно хранить "последнюю" инфу (если так нужно) — заменяем putIfAbsent на put
                    lastInfo.put(u.getUsername(), u);
                }
            }

            UserFlightsInfo fc = null;
            if (fleet.getCharacterId() != null && fleet.getCharacterId() != 0L) {
                fc = fleetTrackerRepository.findUserInfoByCharacterId(fleet.getCharacterId());
            } else if (fleet.getCreatorId() != null) {
                fc = fleetTrackerRepository.findUserInfoByCreatorId(fleet.getCreatorId());
            }

            if (fc != null && fc.getUsername() != null) {
                usernamesInFleet.add(fc.getUsername());
                lastInfo.put(fc.getUsername(), fc);
            }

            // 2) Инкрементим счётчик строго по уникальным именам в рамках флота
            for (String username : usernamesInFleet) {
                counts.computeIfAbsent(username, k -> new LongAdder()).increment();
            }
        }

        List<Map.Entry<String, LongAdder>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Comparator
            .<Map.Entry<String, LongAdder>>comparingLong(e -> e.getValue().longValue()).reversed()
            .thenComparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER)
        );

        Set<UserFleetStat> out = new LinkedHashSet<>();
        long prevValue = Long.MIN_VALUE;
        int rank = 0;

        for (Map.Entry<String, LongAdder> e : sorted) {
            String username = e.getKey();
            long value = e.getValue().longValue();

            if (value != prevValue) {
                rank = rank == 0 ? 1 : rank + 1;
                prevValue = value;
            }

            UserFlightsInfo info = lastInfo.get(username);
            long characterId = info != null ? info.getCharacterId() : 0L;
            boolean tsConnected = info != null && info.isTeamspeakConnected();

            out.add(new UserFleetStat(
                username,
                characterId,
                rank,
                value,
                metric,
                tsConnected
            ));
        }

        return out;
    }


    @Override
    public Set<UserFleetStat> getFleetStats() {
        LocalDate from = LocalDate.of(2017, 1, 1);
        LocalDate to = LocalDate.now().plusDays(2);
        return buildLeaderboard(FleetMetric.CTA_ALL, from, to);
    }
}
