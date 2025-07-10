package com.azarenka.evebuilders.service.impl.jobs;

import com.azarenka.evebuilders.service.api.IContractService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ContractValidatorJob {

    @Autowired
    private IContractService contractService;
    @Autowired
    private IDistributedOrderService distributedOrderService;

    //@Scheduled(cron = "0 * * * * *")
    public void checkContracts() {

    }
}
