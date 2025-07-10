package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.service.impl.contract.ContractValidationReport;

import java.util.List;

public interface IContractService {

    List<ContractValidationReport> getContractReport(DistributedOrder distributedOrder);

    boolean isContractExists(DistributedOrder distributedOrder);
}
