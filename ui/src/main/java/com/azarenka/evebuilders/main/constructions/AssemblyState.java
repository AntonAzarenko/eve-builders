package com.azarenka.evebuilders.main.constructions;

import com.azarenka.evebuilders.domain.dto.ProductionNode;

import java.util.*;

public class AssemblyState {

    private final Map<ProductionNode, Double> efficiencyMap = new HashMap<>();
    private final Map<ProductionNode, Integer> countMap = new HashMap<>();
    private final Set<String> renderedModules = new HashSet<>();
    private final List<ProductionNode> rootNodes = new ArrayList<>();
    private final double baseSotiyoBenefitPercentage = 4.2;

    public void addModule(ProductionNode root, int count) {
        renderedModules.add(root.getTypeName());
        efficiencyMap.put(root, 0d);
        countMap.put(root, count);
        rootNodes.add(root);
    }

    public void removeModule(ProductionNode root) {
        renderedModules.remove(root.getTypeName());
        efficiencyMap.remove(root);
        countMap.remove(root);
        rootNodes.remove(root);
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

    public Map<ProductionNode, Double> getEfficiencyMap() {
        return efficiencyMap;
    }

    public int recalculateBaseValue(ProductionNode node, int value) {
        ProductionNode root = findRoot(node);
        Double blueprintBonus = efficiencyMap.get(node.getParent());
        int count = value;
        if (!root.equals(node)) {
            count = value - (int) Math.round((double) value / 100 * baseSotiyoBenefitPercentage);
            if (blueprintBonus != null && blueprintBonus > 0) {
                count = value - (int) Math.round((double) value / 100 * (blueprintBonus + baseSotiyoBenefitPercentage));
            }
        }
        Integer rootCount = countMap.get(root);
        return count * (rootCount != null ? rootCount : 1);
    }

    private ProductionNode findRoot(ProductionNode node) {
        while (node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }
}
