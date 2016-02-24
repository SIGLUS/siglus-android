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
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryViewHolderTest {

    private PhysicalInventoryViewHolder viewHolder;
    private Product product;
    private String queryKeyWord = null;
    private ExpireDateViewGroup mockedExpireDateView;
    private InventoryViewModel viewModel;

    @Before
    public void setUp() {
        viewHolder = new PhysicalInventoryViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_physical_inventory, null, false));

        mockedExpireDateView = mock(ExpireDateViewGroup.class);
        viewHolder.expireDateViewGroup = mockedExpireDateView;

        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();

        viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();
    }

    @Test
    public void shouldShowBasicProductInfo() {
        viewHolder.populate(viewModel, queryKeyWord);

        assertThat(viewHolder.tvProductName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
        assertThat(viewHolder.tvProductUnit.getText().toString()).isEqualTo("10mg VIA");
        assertThat(viewHolder.etQuantity.getText().toString()).isEqualTo("10");
        assertThat(viewHolder.tvStockOnHandInInventory.getText().toString()).isEqualTo("123");
        assertThat(RobolectricUtils.getErrorTextView(viewHolder.lyQuantity)).isNull();

        verify(mockedExpireDateView).initExpireDateViewGroup(viewModel, false);

        assertThat(viewHolder.tvStockOnHandInInventory.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldHideAddExpiryDateWhenSohIsZero() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_remove_expiry_date_when_soh_is_0_393, true);
        viewModel.setStockOnHand(0);

        viewHolder.populate(viewModel, queryKeyWord);

        verify(mockedExpireDateView).hideAddExpiryDate(true);
    }

    @Test
    public void shouldNotHideAddExpiryDateWhenSohIsZeroAndToggleOff() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_remove_expiry_date_when_soh_is_0_393, false);
        viewModel.setStockOnHand(0);

        viewHolder.populate(viewModel, queryKeyWord);

        verify(mockedExpireDateView, never()).hideAddExpiryDate(anyBoolean());
    }

    @Test
    public void shouldShowErrorWhenStockCardViewModelIsInvalid() {
        InventoryViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("invalid10")
                .setChecked(true)
                .setValid(false)
                .setType("Embalagem")
                .build();
        viewHolder.populate(viewModel, queryKeyWord);

        TextView errorTextView = RobolectricUtils.getErrorTextView(viewHolder.lyQuantity);
        String errorMessage = RuntimeEnvironment.application.getString(R.string.msg_inventory_check_failed);

        assertThat(errorTextView).isNotNull();
        assertThat(errorTextView.getText().toString()).isEqualTo(errorMessage);
    }

    @Test
    public void shouldUpdateViewModelQuantityWhenQuantityFilled() {
        viewHolder.populate(viewModel, queryKeyWord);

        viewHolder.etQuantity.setText("60");

        assertThat(viewModel.getQuantity()).isEqualTo("60");
    }

    @Test
    public void shouldHideAddExpiryDateWhenUserEnterZeroQuantity() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_remove_expiry_date_when_soh_is_0_393, true);

        viewHolder.populate(viewModel, queryKeyWord);

        viewHolder.etQuantity.setText("0");

        verify(mockedExpireDateView).hideAddExpiryDate(true);
    }

    @Test
    public void shouldNotHideAddExpiryDateWhenUserNotEnterQuantity() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_remove_expiry_date_when_soh_is_0_393, true);

        viewModel.setQuantity("");
        viewHolder.populate(viewModel, queryKeyWord);

        verify(mockedExpireDateView).hideAddExpiryDate(false);
    }

    @Test
    public void shouldNotHideAddExpiryDateWhenUserEnterZeroQuantityWhenToggleOff() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_remove_expiry_date_when_soh_is_0_393, false);

        viewHolder.populate(viewModel, queryKeyWord);

        viewHolder.etQuantity.setText("0");

        verify(mockedExpireDateView, never()).hideAddExpiryDate(anyBoolean());
    }
}