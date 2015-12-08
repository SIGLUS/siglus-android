package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.adapter.RnRFormListAdapter;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import roboguice.inject.InjectView;

public class RnRFormViewHolder extends BaseViewHolder {

    public static final int INT_UNSET = 0;
    private final RnRFormListAdapter rnRFormListAdapter;

    @InjectView(R.id.title)
    TextView txTitle;

    @InjectView(R.id.tx_period)
    TextView txPeriod;

    @InjectView(R.id.tx_message)
    TextView txMessage;

    @InjectView(R.id.btn_view)
    TextView btnView;

    @InjectView(R.id.icon)
    ImageView icon;

    @InjectView(R.id.ly_period)
    View lyPeriod;

    @InjectView(R.id.iv_del)
    View ivDelete;

    public RnRFormViewHolder(RnRFormListAdapter rnRFormListAdapter, View inflate) {
        super(inflate);
        this.rnRFormListAdapter = rnRFormListAdapter;
    }

    public void populate(final RnRFormViewModel model, String programCode) {
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_GROUP:
                txTitle.setText(model.getTitle());
                break;
            case RnRFormViewModel.TYPE_DRAFT:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_incomplete_requisition, model.getName())), R.drawable.ic_description, R.color.color_draft_title, model.getForm());
                break;
            case RnRFormViewModel.TYPE_UNSYNC:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_unsynced_requisition, model.getName())), R.drawable.ic_error, R.color.color_red, model.getForm());
                break;
            case RnRFormViewModel.TYPE_HISTORICAL:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done, INT_UNSET, model.getForm());
                btnView.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                btnView.setOnClickListener(new BtnViewClickListener(model, programCode));
                break;
        }
    }

    private void configHolder(String period, Spanned text, int icDescription, int colorDraftTitle, final RnRForm form) {
        txPeriod.setText(period);
        txMessage.setText(text);
        icon.setImageResource(icDescription);
        if (lyPeriod != null && colorDraftTitle != INT_UNSET) {
            lyPeriod.setBackgroundResource(colorDraftTitle);
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
