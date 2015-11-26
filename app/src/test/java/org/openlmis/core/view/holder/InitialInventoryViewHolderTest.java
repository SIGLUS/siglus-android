package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class InitialInventoryViewHolderTest {

    private InitialInventoryViewHolder viewHolder;
    private String queryKeyWord = null;

    @Before
    public void setUp() {
        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_inventory, null, false);
        viewHolder = new InitialInventoryViewHolder(itemView);
    }

    @Test
    public void shouldInitialViewHolder() throws ParseException {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setExpiryDates(newArrayList("28/11/2015"))
                .setQuantity("10")
                .setChecked(true)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);

        String expectedDate = DateUtil.convertDate("28/11/2015", DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);

        assertThat(viewHolder.txQuantity.getText().toString()).isEqualTo("10");
        assertThat(viewHolder.txExpireDate.getText().toString()).isEqualTo(expectedDate);
        assertThat(viewHolder.checkBox.isChecked()).isTrue();
        assertThat(viewHolder.productName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
        assertThat(viewHolder.productUnit.getText().toString()).isEqualTo("Embalagem");

        assertThat(viewHolder.actionDivider.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldShowEditPanelIfCheckboxIsChecked() {

        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setExpiryDates(newArrayList("28/11/2015"))
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);

        assertThat(viewHolder.actionDivider.getVisibility()).isEqualTo(View.GONE);
        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.GONE);

        viewHolder.itemView.performClick();

        assertThat(viewHolder.actionDivider.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldShowErrorMessageWhenViewModelInValidate() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setExpiryDates(newArrayList("28/11/2015"))
                .setQuantity("abc")
                .setChecked(true)
                .setValidate(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);

        TextView errorTextView = RobolectricUtils.getErrorTextView(viewHolder.lyQuantity);

        assertThat(errorTextView).isNotNull();
        assertThat(errorTextView.getText().toString()).isEqualTo(viewHolder.context.getString(R.string.msg_inventory_check_failed));
    }

    @Test
    public void shouldShowDataPickerDialogWhenClickExpireDate() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setExpiryDates(newArrayList("28/11/2015"))
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);

        viewHolder.txExpireDate.performClick();

        assertThat(ShadowAlertDialog.getLatestAlertDialog()).isNotNull();
    }

    @Test
    public void shouldClearQuantityAndExpiryDate() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setExpiryDates(newArrayList("28/11/2015"))
                .setQuantity("10")
                .setChecked(true)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);

        viewHolder.itemView.performClick();

        assertThat(viewModel.getExpiryDates()).isEmpty();
        assertThat(viewModel.getQuantity()).isEmpty();
    }

    @Test
    public void shouldUpdateViewModelQuantityWhenInputFinished() {
        StockCardViewModel viewModel = new StockCardViewModelBuilder(new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build())
                .setQuantity("")
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord);
        viewHolder.itemView.performClick();
        viewHolder.txQuantity.setText("120");

        assertThat(viewModel.getQuantity()).isEqualTo("120");
    }


}