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
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryWithLotViewHolderTest {
    private PhysicalInventoryWithLotViewHolder viewHolder;
    private Product product;
    private String queryKeyWord = null;
    private InventoryViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        viewHolder = new PhysicalInventoryWithLotViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_physical_inventory, null, false));
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
        viewModel = new InventoryViewModelBuilder(product)
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();
    }

    @Test
    public void shouldPopulate() {
        viewHolder.populate(viewModel, queryKeyWord);

        assertThat(viewHolder.tvProductName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
        assertThat(viewHolder.tvProductUnit.getText().toString()).isEqualTo("10mg VIA");
    }

    @Test
    public void shouldSetAdapter() {
        InventoryViewModel inventoryViewModel = new InventoryViewModelBuilder(product)
                .setKitExpectQuantity(20)
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();

        LotMovementViewModel lotMovementViewModel = new LotMovementViewModelBuilder()
                .setMovementType(MovementReasonManager.MovementType.RECEIVE)
                .setQuantity("100")
                .setLotNumber("testLot").build();


        inventoryViewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel, lotMovementViewModel));

        LotMovementAdapter existingLotMovementAdapter = new LotMovementAdapter(inventoryViewModel.getExistingLotMovementViewModelList(),
                inventoryViewModel.getProductName());

        LotMovementAdapter newLotMovementAdapter = new LotMovementAdapter(inventoryViewModel.getExistingLotMovementViewModelList(),
                inventoryViewModel.getProductName());

        viewHolder.populate(inventoryViewModel, queryKeyWord);

        assertThat(existingLotMovementAdapter.getItemCount()).isEqualTo(2);
        assertThat(newLotMovementAdapter.getItemCount()).isEqualTo(2);
    }
}