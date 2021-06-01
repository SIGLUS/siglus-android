package org.openlmis.core.view.adapter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Ignore
public class BulkInitialInventoryAdapterTest {
//    private BulkInitialInventoryAdapter adapter;
    private RecyclerView mRecyclerView;

    @Before
    public void setUp(){
        mRecyclerView = mock(RecyclerView.class);
//        adapter = new BulkInitialInventoryAdapter(new ArrayList<InventoryViewModel>(),null,null);
//        adapter.data = newArrayList(randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel());
    }

    @Test
    public void shouldReturnMinusOneWhenProductListIsEmpty() throws Exception {
        BulkInitialInventoryAdapter adapter = new BulkInitialInventoryAdapter(new ArrayList<InventoryViewModel>(), null, null);
        mRecyclerView.setAdapter(adapter);

        adapter.getItemCount();
        assertThat(adapter.validateAll(), is(-1));
    }

    @Test
    public void shouldReturnUncheckedViewModelPosition() throws Exception {
        List<InventoryViewModel> models = newArrayList(randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel());
        int expectedPosition = nextInt(0, models.size() - 1);
        InventoryViewModel model = models.get(expectedPosition);
        model.setChecked(false);
        BulkInitialInventoryAdapter adapter = new BulkInitialInventoryAdapter(models, null, null);
//        adapter.notifyDataSetChanged();
        adapter.hasObservers();
        adapter.getItemCount();
        assertThat(adapter.validateAll(), is(expectedPosition));
    }

    @Test
    public void shouldReturnMinusOneWhenAllModelsAreChecked () throws Exception {
        List<InventoryViewModel> models = newArrayList(dummyIntentoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel(), randomCheckedInventoryViewModel());
        BulkInitialInventoryAdapter adapter = new BulkInitialInventoryAdapter(models, null, null);
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