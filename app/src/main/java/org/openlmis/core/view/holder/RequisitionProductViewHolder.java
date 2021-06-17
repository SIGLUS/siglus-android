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

package org.openlmis.core.view.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import org.openlmis.core.R;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import roboguice.inject.InjectView;
import rx.Subscriber;

public class RequisitionProductViewHolder extends BaseViewHolder {

  @InjectView(R.id.tx_FNM)
  TextView productCode;

  @InjectView(R.id.tx_product_name)
  TextView productName;

  @InjectView(R.id.iv_del)
  View ivDelete;

  private VIARequisitionPresenter presenter;

  private Context context;

  public RequisitionProductViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final RequisitionFormItemViewModel entry, VIARequisitionPresenter presenter,
      Context context) {
    this.presenter = presenter;
    this.context = context;
    productCode.setText(entry.getFmn());
    productName.setText(entry.getProductName());
    setDeleteIconForNewAddedProducts(entry);
  }

  private void setDeleteIconForNewAddedProducts(final RequisitionFormItemViewModel entry) {
    if (!hideDeleteIconInVIAPage() && isNewAddedProduct(entry)) {
      ivDelete.setVisibility(View.VISIBLE);
      ivDelete.setOnClickListener((v) -> showDelConfirmDialog(entry.getItem()));
    } else {
      ivDelete.setVisibility(View.INVISIBLE);
    }
  }

  private boolean isNewAddedProduct(RequisitionFormItemViewModel entry) {
    return entry.getItem().isManualAdd();
  }

  public Boolean hideDeleteIconInVIAPage() {
    return !(presenter.getRnRForm() != null && presenter.getRnRForm().canRemoveAddedProducts())
        || (presenter.getRnRForm() != null && presenter.getRnRForm().isEmergency());
  }

  protected void showDelConfirmDialog(final RnrFormItem item) {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        context.getString(R.string.label_to_comfirm_delete_product));
    dialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "del_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        presenter.removeRnrItem(item).subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            presenter.deleteRnRItemFromViewModel(item);
          }

          @Override
          public void onError(Throwable e) {
            ToastUtil.show(e.getMessage());
          }

          @Override
          public void onNext(Void aVoid) {

          }
        });
      }

      @Override
      public void negativeClick(String tag) {

      }
    });

  }
}
