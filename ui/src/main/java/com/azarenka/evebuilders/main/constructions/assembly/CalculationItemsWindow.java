package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.common.util.IGridColumnAdder;
import com.azarenka.evebuilders.common.util.VaadinUtils;
import com.azarenka.evebuilders.domain.dto.CalculationItemInformation;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.main.commonview.CommonDialogComponent;
import com.azarenka.evebuilders.main.constructions.api.IBuildConstructionController;
import com.azarenka.evebuilders.service.util.DecimalFormatter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CalculationItemsWindow extends CommonDialogComponent
    implements IGridColumnAdder<CalculationItemInformation>, LocaleChangeObserver {

    private Column<CalculationItemInformation> nameColumn;
    private Column<CalculationItemInformation> requiredColumn;
    private Column<CalculationItemInformation> hasColumn;
    private Column<CalculationItemInformation> excessColumn;
    private Column<CalculationItemInformation> buyColumn;
    private Column<CalculationItemInformation> avgColumn;
    private Column<CalculationItemInformation> sellColumn;
    private Column<CalculationItemInformation> buyBatchColumn;
    private Column<CalculationItemInformation> avgBatchColumn;
    private Column<CalculationItemInformation> sellBatchColumn;

    private ListDataProvider<CalculationItemInformation> dataProvider;
    private Grid<CalculationItemInformation> grid;
    private final IBuildConstructionController controller;
    private List<CalculationItemInformation> calculationItemInformationList;
    private List<CalculationItemInformation> calculationFiltredItemInformationList;
    private List<ProductionNode> productionNodes;
    private Map<String, Integer> stagesMap;

    public CalculationItemsWindow(IBuildConstructionController controller, List<ProductionNode> productionNodes,
                                  Map<String, Integer> stagesMap, String header) {
        super("calculations-window", true);
        setHeader(header);
        this.controller = controller;
        this.productionNodes = productionNodes;
        this.stagesMap = stagesMap;
        setSizeFull();
        setFullscreen(true);
        initList();
        initGrid();
        add(grid);
        getFooter().add(createCloseButton());
    }

    private void setHeader(String header) {
        if (StringUtils.isNotBlank(header)) {
            super.setHeaderTitle("Calculations for " + header);
        } else {
            super.setHeaderTitle("Calculations for stage");
        }
    }

    private void initGrid() {
        dataProvider = DataProvider.ofCollection(calculationFiltredItemInformationList);
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
        nameColumn =
            addComponentColumn(value -> new HorizontalLayout(createInfoUserButton(value),
                new Span(controller.createIcon(value.getTypeName())),
                new Span(value.getTypeName())), "130px", grid);
        requiredColumn =
            addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(BigDecimal.valueOf(calc.getRequiredQuantity())),
                "130px", grid);
        hasColumn =
            addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(BigDecimal.valueOf(calc.getHasQuantity())),
                "130px",
                grid);
        excessColumn =
            addAmountColumn(calc -> DecimalFormatter.formatDecimalValue(
                BigDecimal.valueOf(calc.getHasQuantity() - calc.getRequiredQuantity())), "130px", grid);
        //addDoubleColumn(CalculationItemInformation::getProductPerBatch, "150px", grid);
        //addDoubleColumn(CalculationItemInformation::getProducedQuantity, "100px", grid);
        //addDoubleColumn(CalculationItemInformation::getExcessQuantity, "90px", grid);

        buyColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaBuyPrice()), "90px", grid);
        avgColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaSplitPrice()), "90px", grid);
        sellColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(calc.getJitaSellPrice()), "90px", grid);
        buyBatchColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(
                calcTotal(BigDecimal.valueOf(calc.getRequiredQuantity()), calc.getJitaBuyPrice())), "90px", grid);
        avgBatchColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(
                calcTotal(BigDecimal.valueOf(calc.getRequiredQuantity()), calc.getJitaSplitPrice())), "90px", grid);
        sellBatchColumn =
            addAmountColumn(calc -> DecimalFormatter.formatIsk(
                calcTotal(BigDecimal.valueOf(calc.getRequiredQuantity()), calc.getJitaSellPrice())), "90px", grid);
        updateColumnHeaders();
        HeaderRow topRow = grid.prependHeaderRow();

        //topRow.join(nameColumn).setText("Материал");
        //topRow.join(requiredColumn);
        //topRow.join(hasColumn).setText("В наличии");
        //topRow.join(excessColumn).setText("Остаток");
        HorizontalLayout headerLabel = new HorizontalLayout(new Span("Цена за единицу"));
        HorizontalLayout headerBatchLabel = new HorizontalLayout(new Span("Цена за все"));
        headerLabel.setWidthFull();
        headerLabel.setJustifyContentMode(JustifyContentMode.CENTER);
        headerLabel.addClassName("centered-header");
        headerBatchLabel.setWidthFull();
        headerBatchLabel.setJustifyContentMode(JustifyContentMode.CENTER);
        headerBatchLabel.addClassName("centered-header");
        HeaderCell join = topRow.join(buyColumn, avgColumn, sellColumn);
        join.setComponent(headerLabel);
        HeaderCell batchJoin = topRow.join(buyBatchColumn, avgBatchColumn, sellBatchColumn);
        batchJoin.setComponent(headerBatchLabel);
    }

    private BigDecimal calcTotal(BigDecimal quantity, BigDecimal price) {
        if (Objects.nonNull(quantity) && Objects.nonNull(price)) {
            return quantity.multiply(price);
        }
        return BigDecimal.ZERO;
    }

    private Button createInfoUserButton(CalculationItemInformation calculationItemInformation) {
        var button = VaadinUtils.createLumoButton(VaadinIcon.INFO);
        button.addClickListener(event -> {
            List<CalculationItemInformation> fullInfo = calculationItemInformationList.stream()
                .filter(info -> info.getTypeName().equals(calculationItemInformation.getTypeName()))
                .toList();

        });
        return button;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateColumnHeaders();
    }

    private void updateColumnHeaders() {
        nameColumn.setHeader("Материал");
        requiredColumn.setHeader("Требуется для производства");
        hasColumn.setHeader("В наличии");
        excessColumn.setHeader("Остаток");
        buyColumn.setHeader("Продажа");
        avgColumn.setHeader("Среднее");
        sellColumn.setHeader("Покупка");
        buyBatchColumn.setHeader("Продажа");
        avgBatchColumn.setHeader("Среднее");
        sellBatchColumn.setHeader("Покупка");
    }

    private void initList() {
        calculationItemInformationList = controller.collectInformation(productionNodes, stagesMap);
        calculationFiltredItemInformationList = mergeByTypeName(calculationItemInformationList);
    }

    public List<CalculationItemInformation> mergeByTypeName(List<CalculationItemInformation> list) {
        return list.stream()
            .collect(Collectors.toMap(
                CalculationItemInformation::getTypeName,
                item -> item,
                (item1, item2) -> {
                    item1.setRequiredQuantity(item1.getRequiredQuantity() + item2.getRequiredQuantity());
                    item1.setHasQuantity(item1.getHasQuantity() + item2.getHasQuantity());
                    return item1;
                }
            ))
            .values()
            .stream()
            .sorted(Comparator.comparing(CalculationItemInformation::getTypeName))
            .collect(Collectors.toList());
    }
}
