package org.openlmis.core.view.holder;

import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class UnpackKitViewHolderNewTest {
    private UnpackKitViewHolderNew viewHolder;
    private Product product;

    @Before
    public void setUp() throws Exception {
        viewHolder = new UnpackKitViewHolderNew(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_lots_for_unpackit, null, false));
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
    }

    @Test
    public void shouldPopulate() {
        InventoryViewModel inventoryViewModel = new UnpackKitInventoryViewModel(product);
        inventoryViewModel.setKitExpectQuantity(20);
        inventoryViewModel.setQuantity("20");
        inventoryViewModel.setChecked(false);
        inventoryViewModel.setType("Embalagem");
        inventoryViewModel.setStockOnHand(123L);

        viewHolder.populate(inventoryViewModel);

        assertThat(viewHolder.tvKitExpectedQuantity.getText().toString()).isEqualTo("20 expected");
    }

    @Test
    public void shouldSetAdapter() {
        InventoryViewModel inventoryViewModel = new UnpackKitInventoryViewModel(product);
        inventoryViewModel.setKitExpectQuantity(20);
        inventoryViewModel.setQuantity("20");
        inventoryViewModel.setChecked(false);
        inventoryViewModel.setType("Embalagem");
        inventoryViewModel.setStockOnHand(123L);

        LotMovementViewModel lotMovementViewModel = new LotMovementViewModelBuilder()
                .setMovementType(MovementReasonManager.MovementType.RECEIVE)
                .setQuantity("100")
                .setLotNumber("testLot").build();


        inventoryViewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel, lotMovementViewModel));

        LotMovementAdapter existingLotMovementAdapter = new LotMovementAdapter(inventoryViewModel.getExistingLotMovementViewModelList(),
                inventoryViewModel.getProductName());

        LotMovementAdapter newLotMovementAdapter = new LotMovementAdapter(inventoryViewModel.getExistingLotMovementViewModelList(),
                inventoryViewModel.getProductName());

        viewHolder.populate(inventoryViewModel);

        assertThat(existingLotMovementAdapter.getItemCount()).isEqualTo(2);
        assertThat(newLotMovementAdapter.getItemCount()).isEqualTo(2);
    }
}