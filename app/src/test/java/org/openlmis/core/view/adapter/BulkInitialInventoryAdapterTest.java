package org.openlmis.core.view.adapter;

import org.junit.Test;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class BulkInitialInventoryAdapterTest {

    @Test
    public void shouldReturnMinusOneWhenProductListIsEmpty() throws Exception {
        InventoryListAdapter<BaseViewHolder> adapter = new BulkInitialInventoryAdapter(new ArrayList<InventoryViewModel>());
        assertThat(adapter.validateAll(), is(-1));
    }

    @Test
    public void shouldReturnUncheckedViewModelPosition() throws Exception {
        List<InventoryViewModel> models = newArrayList(randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel());
        int expectedPosition = nextInt(0, models.size() - 1);
        InventoryViewModel model = models.get(expectedPosition);
        model.setChecked(false);
        InventoryListAdapter<BaseViewHolder> adapter = new BulkInitialInventoryAdapter(models);
        assertThat(adapter.validateAll(), is(expectedPosition));
    }

    @Test
    public void shouldReturnMinusOneWhenAllModelsAreChecked () throws Exception {
        List<InventoryViewModel> models = newArrayList(dummyIntentoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel());
        InventoryListAdapter<BaseViewHolder> adapter = new BulkInitialInventoryAdapter(models);
        assertThat(adapter.validateAll(), is(-1));
    }

    private InventoryViewModel randomCheckedInventoryViewModel() {
        Product randomProduct = new Product();
        InventoryViewModel inventoryViewModel = new InventoryViewModel(randomProduct);
        inventoryViewModel.setChecked(true);
        return inventoryViewModel;
    }

    private InventoryViewModel dummyIntentoryViewModel() {
        Product dummyProduct = Product.dummyProduct();
        InventoryViewModel inventoryViewModel = new InventoryViewModel(dummyProduct);
        inventoryViewModel.setDummyModel(true);
        inventoryViewModel.setChecked(false);
        return inventoryViewModel;
    }
}