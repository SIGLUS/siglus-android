package org.openlmis.core.view.holder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

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
import rx.functions.Action1;

@RunWith(LMISTestRunner.class)
public class UnpackKitWithLotViewHolderTest {

  private UnpackKitWithLotViewHolder viewHolder;
  private Product product;

  Action1 action1 = new Action1() {
    @Override
    public void call(Object o) {

    }
  };

  @Before
  public void setUp() throws Exception {
    viewHolder = new UnpackKitWithLotViewHolder(LayoutInflater.from(RuntimeEnvironment.application)
        .inflate(R.layout.item_unpack_kit_with_lots, null, false));
    product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40")
        .setStrength("10mg").setType("VIA").build();
  }

  @Test
  public void shouldPopulate() {
    InventoryViewModel inventoryViewModel = new UnpackKitInventoryViewModel(product);
    inventoryViewModel.setKitExpectQuantity(20);
    inventoryViewModel.setChecked(false);
    inventoryViewModel.setType("Embalagem");
    inventoryViewModel.setStockOnHand(123L);

    viewHolder.populate(inventoryViewModel, action1);

    assertThat(viewHolder.tvKitExpectedQuantity.getText().toString()).isEqualTo("20 expected");
  }

  @Test
  public void shouldSetAdapter() {
    InventoryViewModel inventoryViewModel = new UnpackKitInventoryViewModel(product);
    inventoryViewModel.setKitExpectQuantity(20);
    inventoryViewModel.setChecked(false);
    inventoryViewModel.setType("Embalagem");
    inventoryViewModel.setStockOnHand(123L);

    LotMovementViewModel lotMovementViewModel = new LotMovementViewModelBuilder()
        .setMovementType(MovementReasonManager.MovementType.RECEIVE)
        .setQuantity("100")
        .setLotNumber("testLot").build();

    inventoryViewModel.setExistingLotMovementViewModelList(
        newArrayList(lotMovementViewModel, lotMovementViewModel));

    LotMovementAdapter existingLotMovementAdapter = new LotMovementAdapter(
        inventoryViewModel.getExistingLotMovementViewModelList(),
        inventoryViewModel.getProductName());

    LotMovementAdapter newLotMovementAdapter = new LotMovementAdapter(
        inventoryViewModel.getExistingLotMovementViewModelList(),
        inventoryViewModel.getProductName());

    viewHolder.populate(inventoryViewModel, action1);

    assertThat(existingLotMovementAdapter.getItemCount()).isEqualTo(2);
    assertThat(newLotMovementAdapter.getItemCount()).isEqualTo(2);
  }
}