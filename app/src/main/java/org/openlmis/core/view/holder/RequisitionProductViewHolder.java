package org.openlmis.core.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
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

    public RequisitionProductViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final RequisitionFormItemViewModel entry, VIARequisitionPresenter presenter) {
        this.presenter = presenter;
        productCode.setText(entry.getFmn());
        productName.setText(entry.getProductName());
        if (entry.getItem().getForm() == null) {
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_add_drugs_to_via_form)) {
                ivDelete.setVisibility(View.VISIBLE);
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDelConfirmDialog(entry.getItem());
                    }
                });
            }
        } else {
            ivDelete.setVisibility(View.INVISIBLE);
        }
    }

    protected void showDelConfirmDialog(final RnrFormItem item) {
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                LMISApp.getContext().getString(R.string.label_to_comfirm_delete_product));
        dialogFragment.show(((Activity) context).getFragmentManager(), "del_confirm_dialog");
        dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
            @Override
            public void positiveClick(String tag) {
                presenter.removeOneNewRnrItems(item).subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        presenter.refreshProductListInVIAForm();
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
