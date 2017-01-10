package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.PopupMenu;
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

public class RnRFormViewHolder extends BaseViewHolder {

    private final RnRFormItemClickListener itemClickListener;

    @InjectView(R.id.tv_period)
    TextView tvPeriod;

    @InjectView(R.id.tv_report_status)
    TextView tvMessage;

    @InjectView(R.id.tv_drug_count)
    ViewStub vsDrugCount;
    TextView tvDrugCount;


    @InjectView(R.id.btn_report_entry)
    TextView btnView;

    @InjectView(R.id.iv_del)
    View ivDelete;

    public RnRFormViewHolder(View inflate, RnRFormItemClickListener itemClickListener) {
        super(inflate);
        this.itemClickListener = itemClickListener;
    }

    public void populate(final RnRFormViewModel model) {
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_MISSED_PERIOD:
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_previous_period_missing)), R.drawable.ic_description, R.color.color_draft_title);
                break;
            case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_missed_period)), R.drawable.ic_description, R.color.color_draft_title);
                setupButton(model, context.getString(R.string.btn_select_close_period));
                setupButtonColor();
                break;
            case RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY:
                showCannotDoMonthlyInventory(model);
                break;
            case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
                if (isOfMmia(model)) {
                    configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_uncompleted_mmia_physical_inventory_message)), R.drawable.ic_description, R.color.color_draft_title);
                } else {
                    configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_uncompleted_physical_inventory_message, model.getName())), R.drawable.ic_description, R.color.color_draft_title);
                }
                setupButton(model, context.getString(R.string.btn_view_uncompleted_physical_inventory));
                break;
            case RnRFormViewModel.TYPE_INVENTORY_DONE:
            case RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED:
                populateRnrFormNotBeCreatedView(model);
                break;
            case RnRFormViewModel.TYPE_CREATED_BUT_UNCOMPLETED:
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_incomplete_requisition, model.getName())), R.drawable.ic_description, R.color.color_draft_title);
                setupButton(model, context.getString(R.string.btn_view_incomplete_requisition, model.getName()));
                break;
            case RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL:
                populateRnrFormUnsyncedMessage(model);
                break;
            case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
                populateSyncedHistorical(model);
                break;
        }
    }

    private void showCannotDoMonthlyInventory(RnRFormViewModel model) {
        boolean isTraining = LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training);
        if (isOfMmia(model)) {
            if (isTraining) {
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_training_can_not_create_mmia_rnr)), R.drawable.ic_description, R.color.color_draft_title);
            } else {
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_can_not_create_mmia_rnr, DateUtil.getMonthAbbrByDate(model.getPeriodEndMonth().toDate()))), R.drawable.ic_description, R.color.color_draft_title);
            }
        } else {
            if (isTraining) {
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_training_can_not_create_via_rnr)), R.drawable.ic_description, R.color.color_draft_title);
            } else {
                configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_can_not_create_via_rnr, DateUtil.getMonthAbbrByDate(model.getPeriodEndMonth().toDate()))), R.drawable.ic_description, R.color.color_draft_title);
            }
        }
    }

    private void populateRnrFormNotBeCreatedView(RnRFormViewModel model) {
        if (isOfMmia(model)) {
            configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_completed_mmia_physical_inventory_message)), R.drawable.ic_description, R.color.color_draft_title);
        } else {
            configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_completed_physical_inventory_message, model.getName())), R.drawable.ic_description, R.color.color_draft_title);
        }
        setupButton(model, context.getString(R.string.btn_view_completed_physical_inventory, model.getName()));
        setupButtonColor();
    }

    private void populateRnrFormUnsyncedMessage(RnRFormViewModel model) {
        String error;
        if (isOfMmia(model)) {
            error = context.getString(R.string.label_unsynced_mmia_requisition);
        } else {
            error = context.getString(R.string.label_unsynced_requisition, model.getName());
        }

        if (model.getSyncServerErrorMessage() != null) {
            error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
        }
        configHolder(model.getTitle(), Html.fromHtml(error), R.drawable.ic_error, R.color.color_red);
    }

    private boolean isOfMmia(RnRFormViewModel model) {
        return model.getProgramCode().equals(Constants.MMIA_PROGRAM_CODE);
    }

    private void populateSyncedHistorical(RnRFormViewModel model) {
        RnRForm form = model.getForm();
        if (isOfMmia(model)) {
            configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_mmia_submitted_message, model.getSyncedTime())), R.drawable.ic_done_green, R.color.color_white);
        } else {
            configHolder(model.getTitle(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedTime())), R.drawable.ic_done_green, R.color.color_white);
        }
        showDeleteMenu(form);
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
        btnView.setTextColor(context.getResources().getColor(R.color.color_white));
    }

    private void setupButton(RnRFormViewModel model, String buttonText) {
        btnView.setText(buttonText);
        btnView.setOnClickListener(new BtnViewClickListener(model));
        btnView.setEnabled(true);
    }

    private void configHolder(String period, Spanned text, int icDescription, int colorDraftTitle) {
        tvPeriod.setText(period);
        tvPeriod.setCompoundDrawablesWithIntrinsicBounds(icDescription, 0, 0, 0);
        tvPeriod.setBackgroundResource(colorDraftTitle);
        tvMessage.setText(text);

        if (ivDelete != null) {
            ivDelete.setVisibility(View.GONE);
        }
    }

    private void showDeleteMenu(final RnRForm form) {
        ivDelete.setVisibility(View.VISIBLE);

        if (itemClickListener != null) {
            ivDelete.setOnClickListener(new SingleClickButtonListener() {
                @Override
                public void onSingleClick(View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.inflate(R.menu.menu_rnr_list_item);
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            itemClickListener.deleteForm(form);
                            return false;
                        }
                    });
                }
            });
        }
    }

    public interface RnRFormItemClickListener {
        void deleteForm(RnRForm form);

        void clickBtnView(RnRFormViewModel model);
    }

    private class BtnViewClickListener extends SingleClickButtonListener {

        private final RnRFormViewModel model;

        public BtnViewClickListener(final RnRFormViewModel model) {
            this.minClickInterval = 1000;
            this.model = model;
        }

        @Override
        public void onSingleClick(View v) {
            if (itemClickListener != null) {
                btnView.setEnabled(false);
                itemClickListener.clickBtnView(model);
            }
        }
    }
}
