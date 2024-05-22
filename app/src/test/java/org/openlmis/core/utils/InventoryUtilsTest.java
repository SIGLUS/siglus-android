package org.openlmis.core.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;

import com.viethoa.models.AlphabetItem;

import org.junit.Test;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtilsTest {

    @Test
    public void shouldReturnEmptyListWhenNoInventory() {
        assertEquals(0, InventoryUtils.getAlphabetItemsByInventories(null).size());
        assertEquals(0, InventoryUtils.getAlphabetItemsByInventories(new ArrayList<>()).size());
    }

    @Test
    public void shouldReturnNonEmptyListWhenInventoryListIsNotEmpty() {
        String productAName = "AProduct";
        InventoryViewModel mockedInventoryViewModelA = createMockedInventoryViewModel(productAName);
        String productCName = "CProduct";
        InventoryViewModel mockedInventoryViewModelC = createMockedInventoryViewModel(productCName);
        ArrayList<InventoryViewModel> inventoryViewModels = newArrayList(mockedInventoryViewModelA, mockedInventoryViewModelC);
        // action
        List<AlphabetItem> actualAlphabetItems = InventoryUtils.getAlphabetItemsByInventories(inventoryViewModels);
        // verification
        assertEquals(2, actualAlphabetItems.size());

        int aPosition = 0;
        AlphabetItem alphabetItem1 = actualAlphabetItems.get(aPosition);
        assertEquals(aPosition, alphabetItem1.position);
        assertEquals("A", alphabetItem1.word);

        int cPosition = 1;
        AlphabetItem alphabetItem2 = actualAlphabetItems.get(cPosition);
        assertEquals(cPosition, alphabetItem2.position);
        assertEquals("C", alphabetItem2.word);
    }

    @NonNull
    private InventoryViewModel createMockedInventoryViewModel(String productName) {
        InventoryViewModel mockedInventoryViewModel = mock(InventoryViewModel.class);
        when(mockedInventoryViewModel.getProductName()).thenReturn(productName);
        return mockedInventoryViewModel;
    }
}