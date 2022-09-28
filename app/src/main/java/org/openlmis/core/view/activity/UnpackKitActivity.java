/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import org.openlmis.core.view.adapter.UnpackKitAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_kit_unpack)
public class UnpackKitActivity extends BaseActivity {

  public static final String KEY_FROM_UNPACK_KIT_COMPLETED = "unpack kit complete";

  @InjectView(R.id.products_list)
  protected RecyclerView productListRecycleView;

  @InjectView(R.id.tv_total)
  protected TextView tvTotal;

  @InjectView(R.id.tv_total_kit)
  protected TextView tvTotalKit;

  @InjectView(R.id.et_document_number)
  protected EditText etDocumentNumber;

  @InjectView(R.id.vg_kit_document_number)
  protected View documentNumberContainer;

  @InjectPresenter(UnpackKitPresenter.class)
  private UnpackKitPresenter presenter;

  protected UnpackKitAdapter mAdapter;
  private int kitNum;

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_TEAL;
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.UNPACK_KIT_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    kitNum = intent.getIntExtra(Constants.PARAM_KIT_NUM, 0);

    String kitName = intent.getStringExtra(Constants.PARAM_KIT_NAME);
    tvTotalKit.setText(getString(R.string.kit_number, kitNum, kitName));

    initRecyclerView();

    String kitCode = intent.getStringExtra(Constants.PARAM_KIT_CODE);
    Subscription subscription = presenter.getKitProductsObservable(kitCode, kitNum)
        .subscribe(loadViewModelSubscriber);
    subscriptions.add(subscription);
  }

  private void initRecyclerView() {
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new UnpackKitAdapter(presenter.getInventoryViewModels(), signDialogListener);
    productListRecycleView.setAdapter(mAdapter);
    productListRecycleView.addOnScrollListener(new OnScrollListener() {

      int previousState = RecyclerView.SCROLL_STATE_SETTLING;

      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        previousState = newState;
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (previousState == RecyclerView.SCROLL_STATE_DRAGGING && dy != 0) {
          KeyboardUtil.hideKeyboard(recyclerView);
        }
      }
    });
  }

  Subscriber<List<InventoryViewModel>> loadViewModelSubscriber = new Subscriber<List<InventoryViewModel>>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      ToastUtil.show(e.getMessage());
      loaded();
    }

    @Override
    public void onNext(List<InventoryViewModel> inventoryViewModels) {
      mAdapter.refresh();
      setTotal(inventoryViewModels.size());
      loaded();
    }

    private void setTotal(int total) {
      tvTotal.setText(getString(R.string.label_total, total));
    }
  };


  private final SingleClickButtonListener signDialogListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      if (validateAll()) {
        showSignDialog();
      }
    }

    private void showSignDialog() {
      SignatureDialog signatureDialog = new SignatureDialog();
      signatureDialog.setArguments(
          SignatureDialog.getBundleToMe(getString(R.string.dialog_unpack_kit_signature)));
      signatureDialog.setDelegate(signatureDialogDelegate);
      signatureDialog.show(getSupportFragmentManager(), "signature_dialog_for_unpack_kit");
    }
  };

  protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
    @Override
    public void onSign(String sign) {
      loading();
      Subscription subscription = presenter
          .saveUnpackProductsObservable(kitNum, etDocumentNumber.getText().toString(), sign)
          .subscribe(saveKitSubscriber);
      subscriptions.add(subscription);
    }
  };

  Subscriber<Void> saveKitSubscriber = new Subscriber<Void>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      ToastUtil.show(e.getMessage());
      loaded();
    }

    @Override
    public void onNext(Void object) {
      loaded();
      setResult(Activity.RESULT_OK);
      finish();
    }
  };

  public static Intent getIntentToMe(Context context, String code, int num, String kitName) {
    Intent intent = new Intent(context, UnpackKitActivity.class);
    intent.putExtra(Constants.PARAM_KIT_CODE, code);
    intent.putExtra(Constants.PARAM_KIT_NUM, num);
    intent.putExtra(Constants.PARAM_KIT_NAME, kitName);
    return intent;
  }

  public boolean validateAll() {
    int position = mAdapter.validateAll();
    setFromPage();
    if (position >= 0) {
      productListRecycleView.scrollToPosition(position);
      productListRecycleView.post(() -> {
        etDocumentNumber.clearFocus();
        productListRecycleView.requestFocus();
        Log.d("testFocus", "productListRecycleView request focus");
      });
      return false;
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
        getString(R.string.msg_unpack_kit_back_confirm), getString(R.string.btn_discard),
        getString(R.string.btn_cancel), "on_back_pressed");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        finish();
      }

      @Override
      public void negativeClick(String tag) {
        dialogFragment.dismiss();
      }
    });
    dialogFragment.show(getSupportFragmentManager(), "back_confirm_dialog");
  }

  private void setFromPage() {
    for (InventoryViewModel inventoryViewModel : mAdapter.getData()) {
      for (LotMovementViewModel viewModel : inventoryViewModel.getNewLotMovementViewModelList()) {
        viewModel.setFrom(KEY_FROM_UNPACK_KIT_COMPLETED);
      }
    }
  }
}
