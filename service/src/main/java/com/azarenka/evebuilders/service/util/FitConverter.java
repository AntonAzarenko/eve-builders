package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.ModuleSlot;
import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Module;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class FitConverter {

    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private StaticMaterialLoader materialLoader;

    public Fit convertFromText(String text) {
        if (text == null || text.isBlank()) return null;

        String[] lines = text.split("\n");
        if (lines.length == 0) return null;

        Fit fit = parseHeaderToFit(lines);
        if (Objects.nonNull(fit)) {
            fit.setTextFit(text);
        }
        return fit;
    }

    private Fit parseHeaderToFit(String[] text) {
        String[] header = getHeader(text);
        Fit fit = null;
        if (header.length == 2) {
            InvType invTypeByGroupName = dataService.getInvTypeByTypeName(header[0]);
            if (Objects.nonNull(invTypeByGroupName)) {
                fit = new Fit();
                fit.setId(header[1]);
                fit.setName(header[1]);
                fit.setTypeId(invTypeByGroupName.getTypeID());
                fit.setGroupId(invTypeByGroupName.getGroupId());
            }
        }
        return fit;
    }

    private String[] getHeader(String[] line) {
        return line[0]
                .replace("[", "")
                .replace("]", "")
                .split(",");
    }
}
