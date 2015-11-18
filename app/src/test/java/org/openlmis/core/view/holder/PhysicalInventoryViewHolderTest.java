package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryViewHolderTest {

    private PhysicalInventoryViewHolder viewHolder;
    private Product product;

    @Before
    public void setUp() {
        viewHolder = new PhysicalInventoryViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_physical_inventory, null, false));
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
    }

    @Test
    public void shouldShowBasicProductInfo() {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);

        ExpireDateViewGroup mockedExpireDateView = mock(ExpireDateViewGroup.class);
        viewHolder.expireDateViewGroup = mockedExpireDateView;

        StockCardViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();
        viewHolder.populate(viewModel);

        assertThat(viewHolder.tvProductName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
        assertThat(viewHolder.tvProductUnit.getText().toString()).isEqualTo("10mg VIA");
        assertThat(viewHolder.etQuantity.getText().toString()).isEqualTo("10");
        assertThat(RobolectricUtils.getErrorTextView(viewHolder.lyQuantity)).isNull();

        verify(mockedExpireDateView).initExpireDateViewGroup(viewModel, false);

        assertThat(viewHolder.tvStockOnHandInInventory.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldShowErrorWhenStockCardViewModelIsInvalid() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("invalid10")
                .setChecked(true)
                .setType("Embalagem")
                .setValidate(false)
                .build();
        viewHolder.populate(viewModel);

        TextView errorTextView = RobolectricUtils.getErrorTextView(viewHolder.lyQuantity);
        String errorMessage = RuntimeEnvironment.application.getString(R.string.msg_inventory_check_failed);

        assertThat(errorTextView).isNotNull();
        assertThat(errorTextView.getText().toString()).isEqualTo(errorMessage);
    }

    @Test
    public void shouldUpdateViewModelQuantityWhenQuantityFilled() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(product)
                .setChecked(false)
                .setType("Embalagem")
                .build();
        viewHolder.populate(viewModel);

        viewHolder.etQuantity.setText("60");

        assertThat(viewModel.getQuantity()).isEqualTo("60");
    }

    @Test
    public void shouldSetTvStockOnHandInInventoryNotVisible() {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(false);

        ExpireDateViewGroup mockedExpireDateView = mock(ExpireDateViewGroup.class);
        viewHolder.expireDateViewGroup = mockedExpireDateView;

        StockCardViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();
        viewHolder.populate(viewModel);

        assertThat(viewHolder.tvStockOnHandInInventory.getVisibility()).isEqualTo(View.GONE);
    }
}