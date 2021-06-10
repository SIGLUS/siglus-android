package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.UnpackKitAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_kit_unpack)
public class UnpackKitActivity extends BaseActivity {

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

  private String kitCode;

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
    kitCode = intent.getStringExtra(Constants.PARAM_KIT_CODE);
    kitNum = intent.getIntExtra(Constants.PARAM_KIT_NUM, 0);

    String kitName = intent.getStringExtra(Constants.PARAM_KIT_NAME);
    tvTotalKit.setText(getString(R.string.kit_number, kitNum, kitName));

    initRecyclerView();

    Subscription subscription = presenter.getKitProductsObservable(kitCode, kitNum)
        .subscribe(loadViewModelSubscriber);
    subscriptions.add(subscription);
  }

  private void initRecyclerView() {
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new UnpackKitAdapter(presenter.getInventoryViewModels(), signDialogListener);
    productListRecycleView.setAdapter(mAdapter);
    productListRecycleView.setOnTouchListener((v, event) -> {
      v.requestFocus();
      return false;
    });
  }

  Subscriber<List<InventoryViewModel>> loadViewModelSubscriber = new Subscriber<List<InventoryViewModel>>() {
    @Override
    public void onCompleted() {
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
  };


  private final SingleClickButtonListener signDialogListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      if (validateAll()) {
        showSignDialog();
      }
    }
  };

  private void showSignDialog() {
    SignatureDialog signatureDialog = new SignatureDialog();
    signatureDialog.setArguments(
        SignatureDialog.getBundleToMe(getString(R.string.dialog_unpack_kit_signature)));
    signatureDialog.setDelegate(signatureDialogDelegate);
    signatureDialog.show(getFragmentManager(), "signature_dialog_for_unpack_kit");
  }

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
    }

    @Override
    public void onError(Throwable e) {
      ToastUtil.show(e.getMessage());
      loaded();
    }

    @Override
    public void onNext(Void object) {
      loaded();
      saveSuccess();
    }
  };

  private void setTotal(int total) {
    tvTotal.setText(getString(R.string.label_total, total));
  }

  public static Intent getIntentToMe(Context context, String code, int num, String kitName) {
    Intent intent = new Intent(context, UnpackKitActivity.class);
    intent.putExtra(Constants.PARAM_KIT_CODE, code);
    intent.putExtra(Constants.PARAM_KIT_NUM, num);
    intent.putExtra(Constants.PARAM_KIT_NAME, kitName);
    return intent;
  }

  private void saveSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public boolean validateAll() {
    int position = mAdapter.validateAll();
    if (position >= 0) {
      productListRecycleView.scrollToPosition(position);
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
    dialogFragment.show(getFragmentManager(), "back_confirm_dialog");
  }
}
