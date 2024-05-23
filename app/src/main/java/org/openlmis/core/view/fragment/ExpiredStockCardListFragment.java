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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.openlmis.core.R;
import org.openlmis.core.presenter.ExpiredStockCardListPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.ExpiredStockCardListAdapter;
import org.openlmis.core.view.widget.SignatureWithDateDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class ExpiredStockCardListFragment extends StockCardListFragment {

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
    if (presenter.checkSelectedLotsIsNotEmpty()) {
      showSignDialog();
    } else {
      ToastUtil.show(R.string.expired_products_select_notice);
    }
  }

  private void showSignDialog() {
    SignatureWithDateDialog signatureDialog = new SignatureWithDateDialog();
    signatureDialog.setArguments(SignatureWithDateDialog.getBundleToMe(
        DateUtil.formatDate(DateUtil.getCurrentDate())));
    signatureDialog.hideTitle();
    signatureDialog.setDelegate(presenter);
    signatureDialog.show(getParentFragmentManager());
  }

  @Override
  protected int getStockCardListLayoutId() {
    return R.layout.fragment_expired_stock_card_list;
  }

  @Override
  protected void createAdapter() {
    mAdapter = new ExpiredStockCardListAdapter(new ArrayList<>(), presenter);
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
}