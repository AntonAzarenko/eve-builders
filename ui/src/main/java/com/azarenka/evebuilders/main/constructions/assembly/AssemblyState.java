package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.ProductionNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AssemblyState {

    private final Map<ProductionNode, Double> efficiencyMap = new HashMap<>();
    private final Map<ProductionNode, Integer> countMap = new HashMap<>();
    private final Set<ProductionNode> excludedNodes = new HashSet<>();
    private final Set<ProductionNode> manuallyExcludedNodes = new HashSet<>();
    private final Set<String> renderedModules = new HashSet<>();
    private final List<ProductionNode> rootNodes = new ArrayList<>();
    private final double baseSotiyoBenefitPercentage = 1;
    private final double rigsPercentage = 4.2;
    private final double tataraBenefitPercentage = 2.6;

    private Set<MaterialType> compositeTypes = Set.of(MaterialType.SIMPLE_REACTION, MaterialType.COMPOSITE_REACTION,
        MaterialType.INTERMEDIATE);

    public void addModule(ProductionNode root, int count) {
        renderedModules.add(root.getTypeName());
        // efficiencyMap.put(root, 0d);
        countMap.put(root, count);
        rootNodes.add(root);
    }

    public void removeModule(ProductionNode root) {
        renderedModules.remove(root.getTypeName());
        efficiencyMap.remove(root);
        countMap.remove(root);
        rootNodes.remove(root);
        manuallyExcludedNodes.remove(root);
    }

    public void recalculateRoots() {
        rootNodes.forEach(node -> recalculateTreeQuantities(node, node.getQuantity()));
    }

    public boolean isAlreadyRendered(String moduleName) {
        return renderedModules.contains(moduleName);
    }

    public void setEfficiency(ProductionNode node, double value) {
        efficiencyMap.put(node, value);
    }

    public void setCount(ProductionNode node, int count) {
        countMap.put(node, count);
    }

    public int getCount(ProductionNode node) {
        return countMap.getOrDefault(node, 1);
    }

    public double getEfficiency(ProductionNode node) {
        return efficiencyMap.getOrDefault(node, 0d);
    }

    public List<ProductionNode> getRootNodes() {
        return rootNodes;
    }

    public Map<ProductionNode, Integer> getCountMap() {
        return countMap;
    }

    public Set<ProductionNode> getManuallyExcludedNodes() {
        return manuallyExcludedNodes;
    }

    public Set<ProductionNode> getExcludedNodes() {
        return excludedNodes;
    }

    public Map<ProductionNode, Double> getEfficiencyMap() {
        return efficiencyMap;
    }

    public int recalculateBaseValue(ProductionNode node, int value) {
        ProductionNode root = findRoot(node);
        int q = value;
        Double bp = efficiencyMap.get(node.getParent());
        if (bp != null && bp > 0) {
            q = ceilApply(q, bp);
        }
        if (rigsPercentage > 0) {
            q = ceilApply(q, rigsPercentage);
        }
        if (baseSotiyoBenefitPercentage > 0) {
            q = ceilApply(q, baseSotiyoBenefitPercentage);
        }
        if (node.getMaterialType() != null && compositeTypes.contains(node.getMaterialType())) {
            q = ceilApply(q, tataraBenefitPercentage);
        }
        if (node == root) {
            int rootCount = countMap.getOrDefault(root, 1);
            q *= rootCount;
        }
        return q;
    }

    public void recalculateTreeQuantities(ProductionNode node, int parentAdjustedQuantity) {
        clearEff(node);
        recalculateTreeQuantities(node, parentAdjustedQuantity, false);
    }

    private void recalculateTreeQuantities(ProductionNode node, int parentAdjustedQuantity,
                                           boolean alreadyDiscountedByParent) {
        ProductionNode root = findRoot(node);

        int adjustedQuantity;
        if (node == root) {
            adjustedQuantity = recalculateBaseValue(node, parentAdjustedQuantity);
        } else {
            adjustedQuantity = alreadyDiscountedByParent
                ? parentAdjustedQuantity
                : recalculateBaseValue(node, parentAdjustedQuantity);
        }
        node.setFinalQuantity(adjustedQuantity);
        if (node.getChildren().isEmpty()) {
            return;
        }
        int outPerBatch = Math.max(1, node.getOutputQuantity());
        int parentBatches = (int) Math.ceil(adjustedQuantity / (double) outPerBatch);
        for (ProductionNode child : node.getChildren()) {
            String childType = child.getTypeName();
            int basePerBatch = node.getRecipeQuantityBase(childType);
            int effPerBatch = effPerBatchForEdge(node, childType);
            int childRequired;
            if (effPerBatch == basePerBatch) {
                int rawTotal = parentBatches * basePerBatch;       // 1*22 или 2*22 и т.д.
                int discountedTot = applyAllBonusesToTotal(node, rawTotal); // -> 21 или 42
                childRequired = discountedTot;
                int effByTotal = (int) Math.ceil(discountedTot / (double) parentBatches);
                node.putRecipePerBatchEff(childType, effByTotal);
            } else {
                childRequired = parentBatches * effPerBatch;
                node.putRecipePerBatchEff(childType, effPerBatch);
            }
            recalculateTreeQuantities(child, childRequired, true);
        }
    }

    ProductionNode findRoot(ProductionNode node) {
        while (node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }

    public List<ProductionNode> findAllNodesByName(ProductionNode root, String targetName) {
        List<ProductionNode> result = new ArrayList<>();
        collectByNameRecursive(root, targetName, result);
        return result;
    }

    private void collectByNameRecursive(ProductionNode node, String targetName, List<ProductionNode> result) {
        if (node.getTypeName().equalsIgnoreCase(targetName)) {
            result.add(node);
        }

        for (ProductionNode child : node.getChildren()) {
            collectByNameRecursive(child, targetName, result);
        }
    }

    public Map<Integer, List<ProductionNode>> buildStageMap(ProductionNode root) {
        Map<Integer, List<ProductionNode>> stageMap = new HashMap<>();
        Set<ProductionNode> visited = new HashSet<>();
        Queue<ProductionNode> queue = new LinkedList<>();

        queue.add(root);

        while (!queue.isEmpty()) {
            ProductionNode node = queue.poll();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            int stage = node.getStage();
            stageMap.computeIfAbsent(stage, k -> new ArrayList<>()).add(node);

            for (ProductionNode child : node.getChildren()) {
                queue.add(child);
            }
        }
        return stageMap;
    }

    public TreeMap<Integer, Map<String, Integer>> calculateRealQuantities(Map<Integer, List<ProductionNode>> stageMap) {
        TreeMap<Integer, Map<String, Integer>> result = new TreeMap<>();
        for (Map.Entry<Integer, List<ProductionNode>> stageEntry : stageMap.entrySet()) {
            int stage = stageEntry.getKey();
            List<ProductionNode> nodes = stageEntry.getValue(); // не фильтруем — чтобы root-excluded попали в UI
            Map<String, Integer> typeMap = new HashMap<>();
            Map<String, List<ProductionNode>> groupedByType =
                nodes.stream().collect(Collectors.groupingBy(ProductionNode::getTypeName));
            for (Map.Entry<String, List<ProductionNode>> groupEntry : groupedByType.entrySet()) {
                String typeName = groupEntry.getKey();
                List<ProductionNode> sameTypeNodes = groupEntry.getValue();
                boolean groupContainsRootExcluded = sameTypeNodes.stream().anyMatch(this::isRootExcluded);
                int requiredQtyFromParents = 0;
                if (stage > 0) {
                    List<ProductionNode> possibleParents = stageMap.get(stage - 1);
                    if (possibleParents != null) {
                        List<ProductionNode> parents = possibleParents.stream()
                            .filter(p -> !isExcludedForCalc(p))
                            .filter(p -> p.getChildren().stream()
                                .anyMatch(c -> !isExcludedForCalc(c) && c.getTypeName().equals(typeName)))
                            .toList();
                        Map<String, List<ProductionNode>> groupedParents = parents.stream()
                            .collect(Collectors.groupingBy(ProductionNode::getTypeName));
                        for (Map.Entry<String, List<ProductionNode>> pg : groupedParents.entrySet()) {
                            List<ProductionNode> group = pg.getValue();
                            if (group.isEmpty()) {
                                continue;
                            }
                            ProductionNode sampleParent = group.get(0);
                            int outputQty = Math.max(1, sampleParent.getOutputQuantity());
                            int basePerBatch = sampleParent.getRecipeQuantityBase(typeName);
                            int effPerBatch = sampleParent.getEffectivePerBatch(typeName);
                            int totalDemand = group.stream()
                                .mapToInt(ProductionNode::getFinalQuantity)
                                .sum();
                            int batches = (int) Math.ceil(totalDemand / (double) outputQty);
                            if (effPerBatch == basePerBatch) {
                                // скидка на per-batch не «пробила» ceil -> дожимаем на сумме
                                int rawTotal = batches * basePerBatch;
                                int discounted = applyAllBonusesToTotal(sampleParent, rawTotal);
                                requiredQtyFromParents += discounted;
                            } else {
                                // обычный путь: скидка уже в per-batch учтена
                                requiredQtyFromParents += batches * effPerBatch;
                            }
                        }
                    }
                } else {
                    requiredQtyFromParents = sameTypeNodes.stream()
                        .filter(n -> !isExcludedForCalc(n))
                        .mapToInt(ProductionNode::getFinalQuantity)
                        .sum();
                }
                int requiredQty;
                if (requiredQtyFromParents > 0) {
                    requiredQty = requiredQtyFromParents;
                } else if (groupContainsRootExcluded) {
                    requiredQty = sameTypeNodes.stream()
                        .filter(this::isRootExcluded)
                        .mapToInt(ProductionNode::getFinalQuantity)
                        .sum();
                } else {
                    continue;
                }
                typeMap.put(typeName, requiredQty);
            }
            if (!typeMap.isEmpty()) {
                result.put(stage, typeMap);
            }
        }
        return result;
    }

    public boolean isRootExcluded(ProductionNode n) {
        return getManuallyExcludedNodes().contains(n);
    }

    public boolean isAutoExcluded(ProductionNode n) {
        return getExcludedNodes().contains(n);
    }

    public boolean isExcludedForCalc(ProductionNode n) {
        return isRootExcluded(n) || isAutoExcluded(n);
    }

    private static int ceilApply(int value, double pct) {
        if (pct <= 0) {
            return value;
        }
        return (int) Math.ceil(value * (1.0 - pct / 100.0));
    }

    /**
     * Считает per-batch с бонусами РОДИТЕЛЯ для ребра parent -> child.
     * Порядок важен: BPO -> структура+риги -> татара. После каждого шага делаем ceil.
     */
    private int effPerBatchForEdge(ProductionNode parent, String childTypeName) {
        int q = parent.getRecipeQuantityBase(childTypeName); // чистый per-batch из чертежа
        Double bp = efficiencyMap.get(parent);
        if (bp != null && bp > 0) {
            q = ceilApply(q, bp);
        }
        if (rigsPercentage > 0) {
            q = ceilApply(q, rigsPercentage);
        }
        if (baseSotiyoBenefitPercentage > 0) {
            q = ceilApply(q, baseSotiyoBenefitPercentage);
        }
        if (parent.getMaterialType() != null && compositeTypes.contains(parent.getMaterialType())) {
            q = ceilApply(q, tataraBenefitPercentage);
        }
        return q;
    }

    private int applyAllBonusesToTotal(ProductionNode parent, int total) {
        int q = total;

        Double bp = efficiencyMap.get(parent);
        if (bp != null && bp > 0) {
            q = ceilApply(q, bp);
        }
        if (rigsPercentage > 0) {
            q = ceilApply(q, rigsPercentage);
        }
        if (baseSotiyoBenefitPercentage > 0) {
            q = ceilApply(q, baseSotiyoBenefitPercentage);
        }
        if (parent.getMaterialType() != null && compositeTypes.contains(parent.getMaterialType())) {
            q = ceilApply(q, tataraBenefitPercentage);
        }
        return q;
    }

    private void clearEff(ProductionNode node) {
        node.clearRecipePerBatchEff();
        for (ProductionNode c : node.getChildren()) {
            clearEff(c);
        }
    }
}
