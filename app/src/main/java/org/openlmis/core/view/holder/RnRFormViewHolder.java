package org.openlmis.core.view.holder;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;

import static org.openlmis.core.R.color.color_white;

public class RnRFormViewHolder extends BaseViewHolder {

    private final RnRFormItemClickListener itemClickListener;

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.tv_report_status)
    TextView tvMessage;

    @InjectView(R.id.tv_drug_count)
    ViewStub vsDrugCount;
    TextView tvDrugCount;


    @InjectView(R.id.btn_create_patient_data_report)
    TextView btnView;

    @InjectView(R.id.iv_del)
    View ivDelete;

    public RnRFormViewHolder(View inflate, RnRFormItemClickListener itemClickListener) {
        super(inflate);
        this.itemClickListener = itemClickListener;
    }

    @SuppressLint("StringFormatMatches")
    public void populate(final RnRFormViewModel model) {
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_MISSED_PERIOD:
                configHolder(model.getTitle(),
                        Html.fromHtml(context.getString(R.string.label_previous_period_missing)),
                        R.drawable.ic_description, R.color.color_draft_title, color_white);
                break;
            case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
                configHolder(model.getTitle(),
                        Html.fromHtml(context.getString(R.string.label_missed_period)),
                        R.drawable.ic_description, R.color.color_select_title, color_white);
                setupButton(model, context.getString(R.string.btn_select_close_period));
                setupButtonColor();
                break;
            case RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY:
                showCannotDoMonthlyInventory(model);
                break;
            case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
                configHolder(model.getTitle(),
                        Html.fromHtml(context.getString(R.string.label_uncompleted_physical_inventory_message, model.getName(), model.getName())),
                        R.drawable.ic_description, R.color.color_draft_title, color_white);
                setupButton(model, context.getString(R.string.btn_view_uncompleted_physical_inventory));
                break;
            case RnRFormViewModel.TYPE_INVENTORY_DONE:
            case RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED:
                populateRnrFormNotBeCreatedView(model);
                break;
            case RnRFormViewModel.TYPE_DRAFT:
                configHolderForUnComplete(model, R.string.label_incomplete_requisition, R.string.btn_view_incomplete_requisition);
                break;
            case RnRFormViewModel.TYPE_SUBMIT:
                configHolderForUnComplete(model, R.string.label_incomplete_requisition, R.string.btn_view_incomplete_requisition);
                break;
            case RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL:
                populateRnrFormUnsyncedMessage(model);
                break;
            case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
                populateSyncedHistorical(model);
                break;
            case RnRFormViewModel.TYPE_INACTIVE:
                populateRnrFormInActiveCreatedView(model);
                break;
        }
    }

    private void configHolderForUnComplete(RnRFormViewModel model, int p, int p2) {
        configHolder(model.getTitle(),
                Html.fromHtml(context.getString(p, model.getName())),
                R.drawable.ic_description, R.color.color_draft_title, color_white);
        setupButton(model, context.getString(p2, model.getName()));
    }

    private void showCannotDoMonthlyInventory(RnRFormViewModel model) {
        boolean isTraining = LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training);
        if (isTraining) {
            configHolder(model.getTitle(),
                    Html.fromHtml(context.getString(R.string.label_training_can_not_create_report_rnr, model.getName())),
                    R.drawable.ic_description, R.color.color_draft_title, color_white);
        } else {
            configHolder(model.getTitle(),
                    Html.fromHtml(context.getString(R.string.label_can_not_create_report_rnr, model.getName(), DateUtil.getMonthAbbrByDate(model.getPeriodEndMonth().toDate()))),
                    R.drawable.ic_description, R.color.color_draft_title, color_white);
        }
    }

    private void populateRnrFormNotBeCreatedView(RnRFormViewModel model) {
        configHolderForUnComplete(model, R.string.label_completed_physical_inventory_message, R.string.btn_view_completed_physical_inventory);
        setupButtonColor();
    }

    private void populateRnrFormInActiveCreatedView(RnRFormViewModel model) {
        String inActive = context.getString(R.string.inactive_status);
        configHolder(context.getString(R.string.inactive),
                Html.fromHtml(inActive),
                R.drawable.ic_error, R.color.color_red, color_white);
    }

    private void populateRnrFormUnsyncedMessage(RnRFormViewModel model) {
        String error;
        error = context.getString(R.string.label_unsynced_requisition, model.getName());
        if (model.getSyncServerErrorMessage() != null) {
            error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
        }
        configHolder(model.getTitle(),
                Html.fromHtml(error),
                R.drawable.ic_error, R.color.color_red, color_white);
    }

    private boolean isOfMmia(RnRFormViewModel model) {
        return model.getProgramCode().equals(Constants.MMIA_PROGRAM_CODE);
    }

    private void populateSyncedHistorical(RnRFormViewModel model) {
        RnRForm form = model.getForm();
        configHolder(model.getTitle(),
                Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedTime())),
                R.drawable.ic_done_green, color_white, R.color.color_text_primary);
        setupButton(model, context.getString(R.string.btn_view_requisition, model.getName()));

        if (form.isEmergency()) {
            showDrugCount(form.getRnrFormItemList().size());
        } else {
            hideDrugCount();
        }
    }

    private void hideDrugCount() {
        if (tvDrugCount != null) {
            tvDrugCount.setVisibility(View.GONE);
        }
    }

    private void showDrugCount(int size) {
        if (tvDrugCount != null) {
            tvDrugCount.setVisibility(View.VISIBLE);
        } else {
            tvDrugCount = (TextView) vsDrugCount.inflate();
        }
        tvDrugCount.setText(Html.fromHtml(LMISApp.getContext().getResources().getQuantityString(R.plurals.label_drug_count_message, size, size)));
    }

    private void setupButtonColor() {
        btnView.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
        btnView.setPadding(60, 5, 60, 0);
        btnView.setTextColor(context.getResources().getColor(color_white));
    }

    private void setupButton(RnRFormViewModel model, String buttonText) {
        btnView.setText(buttonText);
        btnView.setOnClickListener(new BtnViewClickListener(model));
        btnView.setEnabled(true);
    }

    private void configHolder(String period, Spanned text, int icDescription, int colorDraftTitle, int textColor) {
        tvPeriod.setText(period);
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(icDescription, 0, 0, 0);
        tvPeriod.setBackgroundResource(colorDraftTitle);
        tvPeriod.setTextColor(context.getResources().getColor(textColor));
        tvMessage.setText(text);

        if (ivDelete != null) {
            ivDelete.setVisibility(View.GONE);
        }
    }

    public interface RnRFormItemClickListener {
        void deleteForm(RnRForm form);

        void clickBtnView(RnRFormViewModel model, View view);
    }

    private class BtnViewClickListener extends SingleClickButtonListener {

        private final RnRFormViewModel model;

        public BtnViewClickListener(final RnRFormViewModel model) {
            setMinClickInterval(1000);
            this.model = model;
        }

        @Override
        public void onSingleClick(View v) {
            if (itemClickListener != null) {
                btnView.setEnabled(false);
                itemClickListener.clickBtnView(model, v);
            }
        }
    }
}
