package com.azarenka.evebuilders.service.impl.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ContractValidatorJob {

    @Scheduled(fixedDelay = 2 * 60 * 60 * 1000) // каждые 2 часа
    public void checkContracts() {
        //TODO will implement later
    }
}
