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

package org.openlmis.core.view.fragment;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.openlmis.core.R;
import org.openlmis.core.presenter.ExpiredStockCardListPresenter;
import org.openlmis.core.presenter.ExpiredStockCardListPresenter.ExpiredStockCardListView;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.ExpiredStockCardListAdapter;
import org.openlmis.core.view.widget.SignatureDialog.DialogDelegate;
import org.openlmis.core.view.widget.SignatureWithDateDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class ExpiredStockCardListFragment extends StockCardListFragment implements
    ExpiredStockCardListView {

  private final int PERMISSION_REQUEST_CODE = 200;
  @InjectView(R.id.stock_card_root)
  LinearLayout rootView;

  @InjectView(R.id.divider)
  View divider;

  @InjectView(R.id.action_panel)
  View actionPanel;

  @InjectView(R.id.btn_complete)
  public Button btnDone;

  @InjectView(R.id.btn_save)
  public View btnSave;

  @Inject
  ExpiredStockCardListPresenter presenter;

  private ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
      new RequestPermission(), isGranted -> {
        if (isGranted) {
          showSignDialog();
        } else {
          showPermissionRationale(getActivity());
        }
      });

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    rootView.setBackgroundResource(R.color.general_background_color);
    sortSpinner.setVisibility(View.GONE);
    productsUpdateBanner.setVisibility(View.GONE);
    divider.setVisibility(View.GONE);

    btnSave.setVisibility(View.GONE);
    btnDone.setText(R.string.expired_products_confirm_return_or_remove);
    btnDone.setOnClickListener(new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        onCompleteClick();
      }
    });
  }

  private void onCompleteClick() {
    btnDone.setEnabled(false);
    if (presenter.isCheckedLotsExisting()) {
      checkWritePermission();
    } else {
      ToastUtil.show(R.string.expired_products_select_notice);
    }
    btnDone.setEnabled(true);
  }

  private void showSignDialog() {
    SignatureWithDateDialog signatureDialog = new SignatureWithDateDialog();
    signatureDialog.setArguments(SignatureWithDateDialog.getBundleToMe(
        DateUtil.formatDate(DateUtil.getCurrentDate())));
    signatureDialog.hideTitle();
    signatureDialog.setDelegate(new DialogDelegate() {
      @Override
      public void onSign(String sign) {
        presenter.handleCheckedExpiredProducts(sign);

        FragmentActivity activity = getActivity();
        if (activity != null) {
          activity.setResult(RESULT_OK);
        }
      }
    });
    signatureDialog.show(getParentFragmentManager());
  }

  private void checkWritePermission() {
    FragmentActivity activity = getActivity();
    if (activity == null) {
      return;
    }
    int permissionCheck = ContextCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      showSignDialog();
    } else {
      if (ActivityCompat.shouldShowRequestPermissionRationale(activity, WRITE_EXTERNAL_STORAGE)) {
        showPermissionRationale(activity);
      } else {
        permissionLauncher.launch(WRITE_EXTERNAL_STORAGE);
        requestWriteStoragePermission(activity);
      }
    }
  }

  private void requestWriteStoragePermission(FragmentActivity activity) {
    ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE},
        PERMISSION_REQUEST_CODE);
  }

  private void showPermissionRationale(FragmentActivity activity) {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
    alertBuilder.setCancelable(true);
    alertBuilder.setTitle(getString(R.string.expired_products_request_storage_permission_title));
    alertBuilder.setMessage(
        getString(R.string.expired_products_request_storage_permission_message)
    );
    alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
          requestWriteStoragePermission(activity);
        }
    );
    AlertDialog alert = alertBuilder.create();
    alert.show();
  }

  @Override
  protected int getStockCardListLayoutId() {
    return R.layout.fragment_expired_stock_card_list;
  }

  @Override
  protected void createAdapter() {
    mAdapter = new ExpiredStockCardListAdapter(new ArrayList<>());
  }

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  public void loadStockCards() {
    presenter.loadExpiredStockCards();
  }

  @Override
  protected boolean isFastScrollEnabled() {
    return true;
  }

  @Override
  public void showHandleCheckedExpiredResult(String excelPath) {
    ToastUtil.show(getString(R.string.expired_products_removed_success, excelPath));
  }
}