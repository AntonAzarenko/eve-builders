package com.azarenka.evebuilders.domain.mysql;

import com.azarenka.evebuilders.domain.ModuleSlot;
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
    private ModuleSlot moduleSlot;
    @ManyToOne
    @JoinColumn(name = "fit_id", nullable = false)
    private Fit fit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ModuleSlot getModuleSlot() {
        return moduleSlot;
    }

    public void setModuleSlot(ModuleSlot moduleSlot) {
        this.moduleSlot = moduleSlot;
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
