package com.azarenka.evebuilders.domain.db;

import com.azarenka.evebuilders.domain.enums.ModuleSlotEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "module", schema = "builders")
public class Module {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "name")
    private String moduleName;
    @Column(name = "moduleType", nullable = false)
    private ModuleSlotEnum moduleSlotEnum;
    @ManyToOne
    @JoinColumn(name = "fit_id", nullable = false)
    private Fit fit;

    public Module() {
    }

    public Module(String id, String moduleName, ModuleSlotEnum moduleSlotEnum) {
        this.id = id;
        this.moduleName = moduleName;
        this.moduleSlotEnum = moduleSlotEnum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ModuleSlotEnum getModuleSlot() {
        return moduleSlotEnum;
    }

    public void setModuleSlot(ModuleSlotEnum moduleSlotEnum) {
        this.moduleSlotEnum = moduleSlotEnum;
    }

    public Fit getFit() {
        return fit;
    }

    public void setFit(Fit fit) {
        this.fit = fit;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
