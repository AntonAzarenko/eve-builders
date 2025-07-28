package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.service.util.DecimalFormatter;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculationItemsWindow extends CommonDialogComponent
    implements IGridColumnAdder<CalculationItemInformation>, LocaleChangeObserver {

    private ListDataProvider<CalculationItemInformation> dataProvider;
    private Grid<CalculationItemInformation> grid;
    private final BuilderConstructionController controller;
    private final AssemblyState assemblyState;

    public CalculationItemsWindow(BuilderConstructionController controller, AssemblyState assemblyState) {
        super();
        this.controller = controller;
        this.assemblyState = assemblyState;
        setSizeFull();
        initGrid();
        add(grid);
        getFooter().add(createCloseButton());
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(controller.collectInformation(assemblyState.getRootNodes().stream()
            .flatMap(assemblyState::deepStream)
            .toList(), assemblyState.getStagesMap().values().stream()
            .flatMap(innerMap -> innerMap.values().stream())
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Integer::sum
            ))));
        grid = VaadinUtils.initGrid(dataProvider, "calculation-info-grid");
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        addColumns();
        grid.getColumns().forEach(shipOrderDtoColumn -> {
            shipOrderDtoColumn.setSortable(true);
            shipOrderDtoColumn.setResizable(true);
        });
        var selectionModel = grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }

    private void addColumns() {
        addComponentColumn(value -> new HorizontalLayout(new Span(controller.createIcon(value.getTypeName())), new Span(value.getTypeName())), "130px", grid);
        addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(BigDecimal.valueOf(calc.getRequiredQuantity())), "130px", grid);
        addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(BigDecimal.valueOf(calc.getHasQuantity())), "130px", grid);
        addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(BigDecimal.valueOf(calc.getHasQuantity() - calc.getRequiredQuantity())), "130px", grid);
        addDoubleColumn(CalculationItemInformation::getProductPerBatch, "150px", grid);
        addDoubleColumn(CalculationItemInformation::getProducedQuantity, "100px", grid);
        addDoubleColumn(CalculationItemInformation::getExcessQuantity, "90px", grid);

        addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaBuyPrice()), "90px", grid);
        addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaSplitPrice()), "90px", grid);
        addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaSellPrice()), "90px", grid);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        grid.getColumns().get(0).setHeader("Материал");
        grid.getColumns().get(1).setHeader("Требуется для производства");
        grid.getColumns().get(2).setHeader("В наличии");
        grid.getColumns().get(3).setHeader("Разница");
        grid.getColumns().get(4).setHeader("Прогон");
        grid.getColumns().get(5).setHeader("произведено");
        grid.getColumns().get(6).setHeader("Остаток");

        grid.getColumns().get(7).setHeader("Продажа");
        grid.getColumns().get(8).setHeader("Среднее");
        grid.getColumns().get(9).setHeader("Покупка");

    }
}
