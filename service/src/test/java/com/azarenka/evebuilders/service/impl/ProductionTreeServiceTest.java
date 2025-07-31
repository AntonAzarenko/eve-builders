package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.ProductionTreeCacheKey;
import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.dto.ProductionNode;
import com.azarenka.evebuilders.domain.dto.file.MaterialEntry;
import com.azarenka.evebuilders.domain.dto.file.TypeInfo;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.ProductionTreeCache;
import com.azarenka.evebuilders.service.util.StaticMaterialLoader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionTreeServiceTest {

    @Mock
    private InvTypesRepository repository;
    @Mock
    private EveMaterialsDataService eveMaterialsDataService;
    @Mock
    private StaticMaterialLoader loader;
    @Mock
    private ProductionTreeCache treeCache;
    @InjectMocks
    private ProductionTreeService productionTreeService;

    private final String typeName = "Tritanium";

    @Test
    void testBuildProductionTreeCachedReturnsFromCacheIfExists() {
        ProductionTreeCacheKey key = new ProductionTreeCacheKey(typeName, 100);
        ProductionNode cachedNode = new ProductionNode();
        when(treeCache.contains(key)).thenReturn(true);
        when(treeCache.get(key)).thenReturn(cachedNode);
        ProductionNode result = productionTreeService.buildProductionTreeCached(typeName, 100);
        assertEquals(cachedNode, result);
        verify(treeCache).contains(key);
        verify(treeCache).get(key);
        verifyNoMoreInteractions(treeCache);
    }

    @Test
    void testBuildProductionTreeCachedComputesAndCachesWhenNotInCache() {
        ProductionTreeCacheKey key = new ProductionTreeCacheKey(typeName, 10);
        TypeInfo mockType = new TypeInfo();
        mockType.setOutputQuantity(5);
        mockType.setMaterials(List.of());
        when(treeCache.contains(key)).thenReturn(false);
        when(loader.getByTypeName(typeName)).thenReturn(mockType);
        ProductionNode result = productionTreeService.buildProductionTreeCached(typeName, 10);
        assertEquals(typeName, result.getTypeName());
        assertEquals(10, result.getQuantity());
        assertEquals(10, result.getProducedQuantity());
        assertEquals(0, result.getExcessQuantity());
        verify(treeCache).contains(key);
        verify(loader).getByTypeName(typeName);
        verify(treeCache).put(eq(key), any(ProductionNode.class));
        verifyNoMoreInteractions(treeCache, loader);
    }

    @Test
    void testBuildProductionTreeHandlesNullTypeInfo() {
        when(loader.getByTypeName(typeName)).thenReturn(null);
        ProductionNode result = productionTreeService.buildProductionTree(typeName, 7);
        assertEquals(typeName, result.getTypeName());
        assertEquals(7, result.getQuantity());
        assertEquals(7, result.getProducedQuantity());
        assertEquals(0, result.getExcessQuantity());
        verify(loader).getByTypeName(typeName);
        verifyNoMoreInteractions(loader);
    }

    @Test
    void testBuildProductionTreeHandlesChildren() {
        String materialName = "Pyerite";
        TypeInfo parentType = new TypeInfo();
        parentType.setOutputQuantity(2);
        MaterialEntry materialEntry = new MaterialEntry();
        materialEntry.setMaterialTypeName(materialName);
        materialEntry.setQuantity(3);
        parentType.setMaterials(List.of(materialEntry));
        TypeInfo childType = new TypeInfo();
        childType.setOutputQuantity(1);
        childType.setMaterials(List.of());
        when(loader.getByTypeName(typeName)).thenReturn(parentType);
        when(loader.getByTypeName(materialName)).thenReturn(childType);
        ProductionNode result = productionTreeService.buildProductionTree(typeName, 4);
        assertEquals(4, result.getQuantity());
        assertEquals(4, result.getProducedQuantity());
        assertEquals(0, result.getExcessQuantity());
        assertEquals(1, result.getChildren().size());
        ProductionNode child = result.getChildren().get(0);
        assertEquals(materialName, child.getTypeName());
        assertEquals(6, child.getQuantity()); // 3 * 2 batches
        assertEquals(6, child.getProducedQuantity());
        verify(loader, times(2)).getByTypeName(any());
        verifyNoMoreInteractions(loader);
    }

    @Test
    @Disabled
    void testResolveMaterialsReturnsEmptyForBasicTypes() {
        for (MaterialType type : List.of(
                MaterialType.MINERAL, MaterialType.MOON_MATERIAL,
                MaterialType.ICE_PRODUCT, MaterialType.GAS,
                MaterialType.PLANETARY, MaterialType.UNKNOWN)) {
            MaterialType result = productionTreeService.resolveMaterialType("Rogue Drone Components", 1);
            //assertTrue(result.isEmpty(), "Expected empty for " + type);
        }
        verifyNoInteractions(repository);
    }

    @Test
    @Disabled
    void testResolveMaterialsReturnsFromManufacturingIfExists() {
        MaterialInfo mockMaterial = mock(MaterialInfo.class);
        lenient().when(mockMaterial.getMaterialTypeID()).thenReturn(34);
        lenient().when(mockMaterial.getMaterialName()).thenReturn("Tritanium");
        List<MaterialInfo> expected = List.of(mockMaterial);
        when(repository.findManufacturingMaterials(typeName)).thenReturn(expected);
        MaterialType result = productionTreeService.resolveMaterialType("Rogue Drone Components", 1);
        assertEquals(expected, result);
        verify(repository).findManufacturingMaterials(typeName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @Disabled
    void testResolveMaterialsFallsBackToReactionIfManufacturingEmpty() {
        when(repository.findManufacturingMaterials(typeName)).thenReturn(List.of());
        MaterialInfo fallbackMaterial = mock(MaterialInfo.class);
        List<MaterialInfo> fallback = List.of(fallbackMaterial);
        when(repository.findReactionMaterials(typeName)).thenReturn(fallback);
        MaterialType result = productionTreeService.resolveMaterialType("Rogue Drone Components", 1);
        assertEquals(fallback, result);
        verify(repository).findManufacturingMaterials(typeName);
        verify(repository).findReactionMaterials(typeName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @Disabled
    void testResolveMaterialsUsesReactionForReactionTypes() {
        MaterialInfo mockMaterial = mock(MaterialInfo.class);
        lenient().when(mockMaterial.getMaterialTypeID()).thenReturn(123);
        lenient().when(mockMaterial.getMaterialName()).thenReturn("Fullerite-C60");
        List<MaterialInfo> expected = List.of(mockMaterial);
        when(repository.findReactionMaterials(typeName)).thenReturn(expected);
        MaterialType result = productionTreeService.resolveMaterialType("Rogue Drone Components", 1);
        assertEquals(expected, result);
        verify(repository).findReactionMaterials(typeName);
        verifyNoMoreInteractions(repository);
    }
}