package com.azarenka.evebuilders.service.impl.contract;

import com.azarenka.evebuilders.domain.dto.Contract;

import java.util.ArrayList;
import java.util.List;

public class ContractValidationReport {

    private boolean valid = true;
    private List<String> validateErrorMessages = new ArrayList<>();
    private Integer countItems = 0;
    private Contract contract;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getValidateErrorMessages() {
        return validateErrorMessages;
    }

    public void setValidateErrorMessages(List<String> validateErrorMessages) {
        this.validateErrorMessages = validateErrorMessages;
    }

    public void setErrorMessage(final String errorMessage) {
        validateErrorMessages.add(errorMessage);
    }

    public Integer getCountItems() {
        return countItems;
    }

    public void setCountItems(Integer countItems) {
        this.countItems = countItems;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }
}
