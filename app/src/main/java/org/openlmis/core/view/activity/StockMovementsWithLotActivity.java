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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.StockMovementsPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.LotInfoGroup;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_movements)
public class StockMovementsWithLotActivity extends BaseActivity implements
    StockMovementsPresenter.StockMovementView {

  @InjectView(R.id.list_stock_movement)
  ListView stockMovementList;

  @InjectView(R.id.stock_movement_banner)
  View banner;

  @InjectView(R.id.btn_new_movement)
  View btnNewMovement;

  @InjectView(R.id.tv_cmm)
  TextView tvCmm;

  @InjectView(R.id.label_stock_card_info)
  TextView tvLabelStockCardInfo;

  @InjectView(R.id.vg_lot_info_container)
  LotInfoGroup lotInfoGroup;

  @InjectView(R.id.stock_unpack_container)
  View unpackContainer;

  @InjectView(R.id.btn_unpack)
  Button btnUnpack;

  @InjectPresenter(StockMovementsPresenter.class)
  StockMovementsPresenter presenter;

  @Inject
  LayoutInflater layoutInflater;

  private long stockId;
  private String stockName;

  private StockMovementAdapter stockMovementAdapter;
  private boolean isStockCardArchivable;
  private boolean isActivated;
  private boolean isKit;

  List<MovementReasonManager.MovementType> movementTypes;
  SimpleSelectDialogFragment newMovementDialog;


  @Override
  protected ScreenName getScreenName() {
    return ScreenName.STOCK_CARD_MOVEMENT_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    stockId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0);
    stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
    isActivated = getIntent().getBooleanExtra(Constants.PARAM_IS_ACTIVATED, true);
    isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);
    movementTypes = MovementReasonManager.getInstance().getMovementTypes();

    super.onCreate(savedInstanceState);

    loadStockCard();

    initUI();

    if (savedInstanceState == null) {
      presenter.loadStockMovementViewModels();
    }
  }

  private void loadStockCard() {
    try {
      presenter.setStockCard(stockId);
    } catch (LMISException e) {
      ToastUtil.show(R.string.msg_db_error);
      finish();
    }
  }

  @Override
  protected int getThemeRes() {
    return isKit ? R.style.AppTheme_TEAL : super.getThemeRes();
  }

  private void initUI() {
    setTitle(stockName);

    showBanner();

    initRecyclerView();

    SingleClickButtonListener singleClickButtonListener = getSingleClickButtonListener();
    btnUnpack.setOnClickListener(singleClickButtonListener);
    btnNewMovement.setOnClickListener(singleClickButtonListener);

    if (isKit) {
      tvLabelStockCardInfo.setText(getString(R.string.label_validate_period));
    } else {
      tvLabelStockCardInfo.setText(getString(R.string.label_lot_info));
      tvCmm.setText((presenter.getStockCard().getCMM() < 0)
          ? "" : String.valueOf(presenter.getStockCard().getCMM())
          .replaceAll("\\.?0*$", ""));
    }
    updateExpiryDateViewGroup();
  }

  private void initRecyclerView() {
    View headerView = layoutInflater
        .inflate(R.layout.item_stock_movement_header, stockMovementList, false);
    stockMovementList.addHeaderView(headerView);
    stockMovementAdapter = new StockMovementAdapter(presenter.getStockMovementModelList(),
        presenter.getStockCard());
    stockMovementList.setAdapter(stockMovementAdapter);
  }

  private void showBanner() {
    banner.setVisibility(isActivated ? View.GONE : View.VISIBLE);
  }

  @Override
  public void updateArchiveMenus(boolean isArchivable) {
    isStockCardArchivable = isArchivable;
    invalidateOptionsMenu();
  }

  @Override
  public void updateUnpackKitMenu(boolean unpackable) {
    unpackContainer.setVisibility(unpackable ? View.VISIBLE : View.GONE);
  }

  @Override
  public void updateExpiryDateViewGroup() {
    if (!isKit) {
      lotInfoGroup.setVisibility(
          presenter.getStockCard().calculateSOHFromLots() == 0 ? View.INVISIBLE : View.VISIBLE);
      lotInfoGroup.initLotInfoGroup(presenter.getStockCard().getNonEmptyLotOnHandList());
    }
  }

  @Override
  public void onBackPressed() {
    Intent intent = new Intent();
    intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockId);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  @Override
  public void refreshStockMovement() {
    stockMovementAdapter.notifyDataSetChanged();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean isPrepared = super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.action_archive).setVisible(isStockCardArchivable);

    return isPrepared;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_stock_movement, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_history:
        startActivity(
            StockMovementHistoryActivity.getIntentToMe(this, stockId, stockName, false, isKit));
        return true;
      case R.id.action_archive:
        presenter.archiveStockCard();
        ToastUtil.show(getString(R.string.msg_drug_archived));
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void unpackKit() {
    Product product = presenter.getStockCard().getProduct();
    startActivityForResult(SelectUnpackKitNumActivity.getIntentToMe(this,
        product.getPrimaryName(),
        product.getCode(),
        presenter.getStockCard().getStockOnHand()),
        Constants.REQUEST_UNPACK_KIT);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK && (requestCode == Constants.REQUEST_UNPACK_KIT
        || requestCode == Constants.REQUEST_NEW_MOVEMENT_PAGE)) {
      loadStockCard();
      presenter.loadStockMovementViewModels();
    }
  }


  public static Intent getIntentToMe(Context context, InventoryViewModel inventoryViewModel,
      boolean isKit) {
    Intent intent = new Intent(context, StockMovementsWithLotActivity.class);
    intent.putExtra(Constants.PARAM_STOCK_CARD_ID, inventoryViewModel.getStockCardId());
    intent.putExtra(Constants.PARAM_STOCK_NAME,
        inventoryViewModel.getProduct().getFormattedProductName());
    intent.putExtra(Constants.PARAM_IS_ACTIVATED, inventoryViewModel.getProduct().isActive());
    intent.putExtra(Constants.PARAM_IS_KIT, isKit);
    return intent;
  }

  public SingleClickButtonListener getSingleClickButtonListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        switch (v.getId()) {
          case R.id.btn_unpack:
            unpackKit();
            break;
          case R.id.btn_new_movement:
            String[] selections = FluentIterable.from(movementTypes)
                .transform(movementType -> movementType.getDescription()).toArray(String.class);
            newMovementDialog = new SimpleSelectDialogFragment(new MovementTypeOnClickListener(),
                selections);
            newMovementDialog.show(getFragmentManager(), "");
            break;
        }
      }
    };
  }

  class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      if ((movementTypes.get(position).equals(MovementReasonManager.MovementType.ISSUE)
          || movementTypes.get(position).equals(MovementReasonManager.MovementType.NEGATIVE_ADJUST))
          && presenter.getStockCard().calculateSOHFromLots() == 0) {
        ToastUtil.show(R.string.msg_no_lot_for_issue);
      } else {
        startActivityForResult(NewStockMovementActivity.getIntentToMe(
            StockMovementsWithLotActivity.this,
            stockName,
            movementTypes.get(position),
            stockId,
            isKit),
            Constants.REQUEST_NEW_MOVEMENT_PAGE);
      }
      newMovementDialog.dismiss();
    }
  }

}
