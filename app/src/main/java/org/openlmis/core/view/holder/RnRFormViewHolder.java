package org.openlmis.core.view.holder;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.InventoryActivity;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.activity.SelectPeriodActivity;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import roboguice.inject.InjectView;

public class RnRFormViewHolder extends BaseViewHolder {

    public static final int INT_UNSET = 0;
    public static final int DEFAULT_FORM_ID_OF_NOT_AUTHORIZED = 0;
    private final RnRFormListAdapter rnRFormListAdapter;

    @InjectView(R.id.tx_period)
    TextView txPeriod;

    @InjectView(R.id.tx_message)
    TextView txMessage;

    @InjectView(R.id.btn_view_old)
    TextView btnViewOld;

    @InjectView(R.id.btn_view)
    Button btnView;

    @InjectView(R.id.iv_del)
    View ivDelete;

    public RnRFormViewHolder(RnRFormListAdapter rnRFormListAdapter, View inflate) {
        super(inflate);
        this.rnRFormListAdapter = rnRFormListAdapter;
    }

    public void populate(final RnRFormViewModel model, String programCode) {
        if (btnViewOld != null) {
            btnViewOld.setVisibility(View.GONE);
        }

        switch (model.getType()) {
            case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY:
                configHolder(model, R.string.btn_view_uncompleted_physical_inventory, Html.fromHtml(context.getString(R.string.label_uncompleted_physical_inventory_message, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
            case RnRFormViewModel.TYPE_SELECT_CLOSE_OF_PERIOD:
                configHolder(model, R.string.btn_view_select_close_of_period, null);
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
            case RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED:
                configHolder(model, R.string.btn_view_completed_physical_inventory, Html.fromHtml(context.getString(R.string.label_completed_physical_inventory_message, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                btnView.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
                btnView.setTextColor(context.getResources().getColor(R.color.color_white));
                break;
            case RnRFormViewModel.TYPE_UN_AUTHORIZED:
                configHolder(model, R.string.btn_view_incomplete_requisition, Html.fromHtml(context.getString(R.string.label_incomplete_requisition, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
            case RnRFormViewModel.TYPE_UNSYNC:
                String error = context.getString(R.string.label_unsynced_requisition, model.getName());
                if (model.getSyncServerErrorMessage() != null) {
                    error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
                }
                configHolder(model.getPeriod(), Html.fromHtml(error), R.drawable.ic_error, R.color.color_red, model.getForm());
                break;
            case RnRFormViewModel.TYPE_HISTORICAL:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done_green, INT_UNSET, model.getForm());
                btnView.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
        }
    }

    public void populateOld(final RnRFormViewModel model, String programCode) {
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_UN_AUTHORIZED:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_incomplete_requisition_old, model.getName())), R.drawable.ic_description, R.color.color_draft_title, model.getForm());
                break;
            case RnRFormViewModel.TYPE_UNSYNC:
                String error = context.getString(R.string.label_unsynced_requisition, model.getName());
                if (model.getSyncServerErrorMessage() != null) {
                    error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
                }
                configHolder(model.getPeriod(), Html.fromHtml(error), R.drawable.ic_error, R.color.color_red, model.getForm());
                break;
            case RnRFormViewModel.TYPE_HISTORICAL:
                btnView.setVisibility(View.GONE);
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done, INT_UNSET, model.getForm());
                btnViewOld.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                btnViewOld.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
        }
    }

    private void configHolder(RnRFormViewModel model, int btnText, Spanned text) {
        txPeriod.setText(model.getPeriod());
        txPeriod.setBackgroundResource(R.color.color_draft_title);
        txPeriod.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_description, 0, 0, 0);
        txMessage.setText(text);
        ivDelete.setVisibility(View.GONE);
        btnView.setText(context.getString(btnText, model.getName()));
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
            if (model.getType() == RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY) {
                Intent intent = new Intent(context, InventoryActivity.class);
                intent.putExtra(Constants.PARAM_IS_PHYSICAL_INVENTORY, true);
                ((Activity) context).startActivityForResult(intent, Constants.REQUEST_FROM_RNR_LIST_PAGE);
                return;
            }

            if (model.getType() == RnRFormViewModel.TYPE_SELECT_CLOSE_OF_PERIOD) {
                ((Activity)context).startActivityForResult(SelectPeriodActivity.getIntentToMe(context, programCode), Constants.REQUEST_SELECT_PERIOD_END);
                return;
            }

            if (model.getType() == RnRFormViewModel.TYPE_HISTORICAL) {
                if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
                    ((Activity) context).startActivityForResult(MMIARequisitionActivity.getIntentToMe(context, model.getId()), Constants.REQUEST_FROM_RNR_LIST_PAGE);
                } else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)) {
                    ((Activity) context).startActivityForResult(VIARequisitionActivity.getIntentToMe(context, model.getId()), Constants.REQUEST_FROM_RNR_LIST_PAGE);
                }
                return;
            }

            if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
                ((Activity) context).startActivityForResult(MMIARequisitionActivity.getIntentToMe(context, DEFAULT_FORM_ID_OF_NOT_AUTHORIZED), Constants.REQUEST_FROM_RNR_LIST_PAGE);

            } else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)) {
                ((Activity) context).startActivityForResult(VIARequisitionActivity.getIntentToMe(context, DEFAULT_FORM_ID_OF_NOT_AUTHORIZED), Constants.REQUEST_FROM_RNR_LIST_PAGE);
            }

        }
    }
}
