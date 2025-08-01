package com.azarenka.evebuilders.main.constructions.assembly;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.ProductionNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private boolean isEveryBlueprintHasBenefits = false;
    private int everyBlueprintBenefitsCount = 0;

    private Map<ProductionNode, Map<Integer, Map<String, Integer>>> stagesMap = new HashMap<>();
    private Set<MaterialType> compositeTypes = Set.of(MaterialType.SIMPLE_REACTION, MaterialType.COMPOSITE_REACTION,
        MaterialType.INTERMEDIATE);

    public void addModule(ProductionNode root, int count) {
        renderedModules.add(root.getTypeName());
        countMap.put(root, count);
        rootNodes.add(root);
        recalculateStages();
    }

    void recalculateStages() {
        stagesMap.clear();
        rootNodes.forEach(node -> {
            if (isEveryBlueprintHasBenefits) {
                setBenefitsIfHas(node);
            }
            recalculateTreeQuantities(node, node.getQuantity());
            stagesMap.put(node, calculateStages(node));
        });
    }

    Stream<ProductionNode> deepStream(ProductionNode node) {
        return Stream.concat(
            Stream.of(node),
            node.getChildren().stream()
                .flatMap(this::deepStream)
        );
    }

    public boolean isEveryBlueprintHasBenefits() {
        return isEveryBlueprintHasBenefits;
    }

    public void setEveryBlueprintHasBenefits(boolean everyBlueprintHasBenefits) {
        isEveryBlueprintHasBenefits = everyBlueprintHasBenefits;
    }

    public int getEveryBlueprintBenefitsCount() {
        return everyBlueprintBenefitsCount;
    }

    public void setEveryBlueprintBenefitsCount(int everyBlueprintBenefitsCount) {
        this.everyBlueprintBenefitsCount = everyBlueprintBenefitsCount;
    }

    public void removeModule(ProductionNode root) {
        renderedModules.remove(root.getTypeName());
        efficiencyMap.remove(root);
        countMap.remove(root);
        rootNodes.remove(root);
        manuallyExcludedNodes.remove(root);
        recalculateStages();
    }

    public void clearRoots() {
        renderedModules.clear();
        efficiencyMap.clear();
        countMap.clear();
        rootNodes.clear();
        recalculateStages();
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
                int rawTotal = parentBatches * basePerBatch;
                int discountedTot = applyAllBonusesToTotal(node, rawTotal);
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

    public Set<MaterialType> getCompositeTypes() {
        return compositeTypes;
    }

    public void setCompositeTypes(Set<MaterialType> compositeTypes) {
        this.compositeTypes = compositeTypes;
    }

    public Map<Integer, Map<String, Integer>> calculateStages(ProductionNode root) {
        var integerListMap = buildStageMap(root);
        return calculateRealQuantities(integerListMap);
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
                                int rawTotal = batches * basePerBatch;
                                int discounted = applyAllBonusesToTotal(sampleParent, rawTotal);
                                requiredQtyFromParents += discounted;
                            } else {
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
        int q = parent.getRecipeQuantityBase(childTypeName);
        Double bp = efficiencyMap.get(parent);
        boolean isReaction = parent.getMaterialType() != null && compositeTypes.contains(parent.getMaterialType());
        if (bp != null && bp > 0) {
            q = ceilApply(q, bp);
        }
        if (isReaction) {
            q = ceilApply(q, tataraBenefitPercentage);
            return q;
        }
        if (rigsPercentage > 0) {
            q = ceilApply(q, rigsPercentage);
        }
        if (baseSotiyoBenefitPercentage > 0) {
            q = ceilApply(q, baseSotiyoBenefitPercentage);
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

    public void clearEff(ProductionNode node) {
        node.clearRecipePerBatchEff();
        for (ProductionNode c : node.getChildren()) {
            clearEff(c);
        }
    }

    public void setBenefitsIfHas(ProductionNode node) {
        List<ProductionNode> productionNodes = pickEligibleParents(node);
        if (isEveryBlueprintHasBenefits && everyBlueprintBenefitsCount > 0) {
            productionNodes.forEach(parent -> {
                if (!rootNodes.contains(parent)) {
                    setEfficiency(parent, everyBlueprintBenefitsCount);
                }
            });
        } else {
            productionNodes.forEach(pn -> {
                if (!rootNodes.contains(pn)) {
                    efficiencyMap.remove(pn);
                }
            });
        }
        isEveryBlueprintHasBenefits = false;
    }

    public List<ProductionNode> pickEligibleParents(ProductionNode root) {
        List<ProductionNode> all = new ArrayList<>();
        Deque<ProductionNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            ProductionNode n = stack.pop();
            all.add(n);
            for (ProductionNode c : n.getChildren()) {
                stack.push(c);
            }
        }
        Set<MaterialType> composites = getCompositeTypes();
        return all.stream()
            .filter(n -> !n.getChildren().isEmpty())
            .filter(n -> {
                MaterialType mt = n.getMaterialType();
                return mt != null && !composites.contains(mt);
            })
            .toList();
    }

    public Map<ProductionNode, Map<Integer, Map<String, Integer>>> getStagesMap() {
        return stagesMap;
    }

    public void setStagesMap(Map<ProductionNode, Map<Integer, Map<String, Integer>>> stagesMap) {
        this.stagesMap = stagesMap;
    }
}
