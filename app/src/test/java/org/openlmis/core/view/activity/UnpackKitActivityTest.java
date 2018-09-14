package org.openlmis.core.view.activity;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class UnpackKitActivityTest {

    private UnpackKitPresenter mockedPresenter;
    private UnpackKitActivity unpackKitActivity;
    private Product product;
    private InventoryViewModel viewModel;

    @Before
    public void setUp() throws Exception {

        product = new ProductBuilder().setIsKit(false).setCode("productCode1").setPrimaryName("name1").build();
        viewModel = new UnpackKitInventoryViewModel(product);
        viewModel.setChecked(true);
        viewModel.setKitExpectQuantity(300);

        mockedPresenter = mock(UnpackKitPresenter.class);
        when(mockedPresenter.getKitProductsObservable(anyString(), anyByte())).thenReturn(Observable.<List<InventoryViewModel>>empty());
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UnpackKitPresenter.class).toInstance(mockedPresenter);
            }
        });

        Intent intent = new Intent()
                .putExtra(Constants.PARAM_KIT_CODE, "SD0001")
                .putExtra(Constants.PARAM_KIT_NUM, 1);
        unpackKitActivity = Robolectric.buildActivity(UnpackKitActivity.class).newIntent(intent).create().visible().get();
    }

    @Test
    public void shouldLoadKitProductsWithKitCode() throws Exception {
        verify(mockedPresenter).getKitProductsObservable("SD0001", 1);
    }

    @Test
    public void shouldNotShowDialogWhenQuantityIsNotValid() throws Exception {
        viewModel.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2033").setQuantity("").build());

        unpackKitActivity.mAdapter.getData().clear();
        unpackKitActivity.mAdapter.getData().add(viewModel);

        unpackKitActivity.mAdapter.onCreateViewHolder(unpackKitActivity.productListRecycleView, 1).itemView.findViewById(R.id.btn_complete).performClick();

        assertNull(unpackKitActivity.getFragmentManager().findFragmentByTag("signature_dialog_for_unpack_kit"));
    }

    @Test
    public void shouldShowSignatureDialogIfIsValid() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(100000);
        SingleClickButtonListener.isViewClicked = false;

        viewModel.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setExpiryDate("Jan 2033").setQuantity("100").setLotNumber("some lot").build());
        unpackKitActivity.mAdapter.getData().clear();
        unpackKitActivity.mAdapter.getData().add(viewModel);

        unpackKitActivity.mAdapter.onCreateViewHolder(unpackKitActivity.productListRecycleView, 1).itemView.findViewById(R.id.btn_complete).performClick();
        assertNotNull(unpackKitActivity.getFragmentManager().findFragmentByTag("signature_dialog_for_unpack_kit"));
    }
}