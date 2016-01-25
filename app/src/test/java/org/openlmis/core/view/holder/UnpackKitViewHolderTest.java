package org.openlmis.core.view.holder;

import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class UnpackKitViewHolderTest {
    private UnpackKitViewHolder viewHolder;
    private Product product;

    @Before
    public void setUp() {
        viewHolder = new UnpackKitViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_physical_inventory, null, false));
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
    }

    @Test
    public void shouldGetRightEtQuantity() {
        InventoryViewModel firstViewModel = new StockCardViewModelBuilder(product)
                .setKitExpectQuantity(20)
                .setQuantity("20")
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();

        viewHolder.populate(firstViewModel);

        InventoryViewModel secondViewModel = new StockCardViewModelBuilder(product)
                .setKitExpectQuantity(200)
                .setQuantity("150")
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();

        viewHolder.populate(secondViewModel);

        ExpireDateViewGroup mockedExpireDateView = mock(ExpireDateViewGroup.class);
        viewHolder.expireDateViewGroup = mockedExpireDateView;

        assertThat(viewHolder.etQuantity.getText().toString()).isEqualTo("150");
    }

}