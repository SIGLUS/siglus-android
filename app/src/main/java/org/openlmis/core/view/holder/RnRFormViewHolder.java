package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import roboguice.inject.InjectView;

public class RnRFormViewHolder extends BaseViewHolder {

    public static final int INT_UNSET = 0;
    private final RnRFormListAdapter rnRFormListAdapter;

    @InjectView(R.id.tx_period)
    TextView txPeriod;

    @InjectView(R.id.tx_message)
    TextView txMessage;

    @InjectView(R.id.btn_view)
    TextView btnView;

    @InjectView(R.id.iv_del)
    View ivDelete;

    public RnRFormViewHolder(RnRFormListAdapter rnRFormListAdapter, View inflate) {
        super(inflate);
        this.rnRFormListAdapter = rnRFormListAdapter;
    }

    public void populate(final RnRFormViewModel model, String programCode) {
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY:
                configHolder(model, R.string.label_uncompleted_physical_inventory_message, R.string.btn_view_uncompleted_physical_inventory);
                break;
            case RnRFormViewModel.TYPE_COMPLETED_INVENTORY:
                configHolder(model, R.string.label_completed_physical_inventory_message, R.string.btn_view_completed_physical_inventory);
                break;
            case RnRFormViewModel.TYPE_UN_AUTHORIZED:
                configHolder(model, R.string.label_incomplete_requisition, R.string.btn_view_incomplete_requisition);
                break;
            case RnRFormViewModel.TYPE_UNSYNC:
                String error = context.getString(R.string.label_unsynced_requisition, model.getName());
                if (model.getSyncServerErrorMessage() != null) {
                    error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
                }
                configHolder(model.getPeriod(), Html.fromHtml(error), R.drawable.ic_error, R.color.color_red, model.getForm());
                break;
            case RnRFormViewModel.TYPE_HISTORICAL:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done, INT_UNSET, model.getForm());
                btnView.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
        }
    }

    private void configHolder(RnRFormViewModel model, int messageText, int btnText) {
        txPeriod.setText(model.getPeriod());
        txMessage.setText(Html.fromHtml(context.getString(messageText, model.getName())));
        btnView.setText(context.getString(btnText, model.getName()));
        txPeriod.setBackgroundResource(R.color.color_draft_title);
        txPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
    }

    private void configHolder(String period, Spanned text, int icDescription, int colorDraftTitle, final RnRForm form) {
        txPeriod.setText(period);
        txMessage.setText(text);
        txPeriod.setCompoundDrawablesWithIntrinsicBounds(icDescription, 0, 0, 0);
        if (colorDraftTitle != INT_UNSET) {
            txPeriod.setBackgroundResource(colorDraftTitle);
        }

        if (ivDelete != null && rnRFormListAdapter.getFormDeleteListener() != null) {
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rnRFormListAdapter.getFormDeleteListener().delete(form);
                }
            });
        }
    }

    private class BtnViewClickListener implements View.OnClickListener {

        private final RnRFormViewModel model;
        private final String programCode;

        public BtnViewClickListener(final RnRFormViewModel model, String programCode) {
            this.model = model;
            this.programCode = programCode;
        }

        @Override
        public void onClick(View v) {
            if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
                context.startActivity(MMIARequisitionActivity.getIntentToMe(context, model.getId()));
            } else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)) {
                context.startActivity(VIARequisitionActivity.getIntentToMe(context, model.getId()));
            }
        }
    }
}
