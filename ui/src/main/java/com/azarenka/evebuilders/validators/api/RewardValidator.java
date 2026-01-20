package com.azarenka.evebuilders.validators.api;

import com.azarenka.evebuilders.domain.casino.BoxTypeEnum;
import com.azarenka.evebuilders.domain.casino.Reward;
import com.azarenka.evebuilders.rest.exeptions.BadRequestException;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RewardValidator {

    private RewardValidator() {
    }

    public static void validateForSave(Reward r) {
        if (r == null) {
            throw new BadRequestException("Request body is required");
        }
        require(StringUtils.isNotBlank(r.getUid()), "uid must not be blank");
        require(r.getUid().length() <= 64, "uid length must be <= 64");
        require(StringUtils.isNotBlank(r.getTitle()), "title must not be blank");
        require(StringUtils.isNotBlank(r.getValue()), "value must not be blank");
        require(r.getBoxType() != null, "boxType must not be null");
        require(Arrays.stream(BoxTypeEnum.values()).collect(Collectors.toSet()).contains(r.getBoxType()),
            "boxType must be [SMALL_BOX, MEDIUM_BOX, BIG_BOX]");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new BadRequestException(message);
        }
    }
}
