package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.network.SyncErrorsMap;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import roboguice.inject.InjectView;

public class RnRFormViewHolder extends BaseViewHolder {

    public static final int INT_UNSET = 0;

    private final RnRFormItemClickListener itemClickListener;

    @InjectView(R.id.tx_period)
    TextView txPeriod;

    @InjectView(R.id.tx_message)
    TextView txMessage;

    @InjectView(R.id.btn_view)
    Button btnView;

    @InjectView(R.id.iv_del)
    View ivDelete;

    private RnRForm form;

    public RnRFormViewHolder(View inflate, RnRFormItemClickListener itemClickListener) {
        super(inflate);
        this.itemClickListener = itemClickListener;
    }

    public void populate(final RnRFormViewModel model) {
        form = model.getForm();
        switch (model.getType()) {
            case RnRFormViewModel.TYPE_MISSED_PERIOD:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_previous_period_missing)), R.drawable.ic_description, R.color.color_draft_title, form);
                break;
            case RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_missed_period)), R.drawable.ic_description, R.color.color_draft_title, form);
                btnView.setText(R.string.btn_select_close_period);
                btnView.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
                btnView.setTextColor(context.getResources().getColor(R.color.color_white));
                btnView.setOnClickListener(new BtnViewClickListener(model));
                break;
            case RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_can_not_create_rnr, DateUtil.getMonthAbbrByDate(model.getPeriodEndMonth().toDate()))), R.drawable.ic_description, R.color.color_draft_title, form);
                break;
            case RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD:
                configHolder(model, R.string.btn_view_uncompleted_physical_inventory, Html.fromHtml(context.getString(R.string.label_uncompleted_physical_inventory_message, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model));
                break;
            case RnRFormViewModel.TYPE_INVENTORY_DONE:
            case RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED:
                configHolder(model, R.string.btn_view_completed_physical_inventory, Html.fromHtml(context.getString(R.string.label_completed_physical_inventory_message, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model));
                btnView.setBackground(context.getResources().getDrawable(R.drawable.blue_button));
                btnView.setTextColor(context.getResources().getColor(R.color.color_white));
                break;
            case RnRFormViewModel.TYPE_CREATED_BUT_UNCOMPLETED:
                configHolder(model, R.string.btn_view_incomplete_requisition, Html.fromHtml(context.getString(R.string.label_incomplete_requisition, model.getName())));
                btnView.setOnClickListener(new BtnViewClickListener(model));
                break;
            case RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL:
                String error = context.getString(R.string.label_unsynced_requisition, model.getName());
                if (model.getSyncServerErrorMessage() != null) {
                    error = SyncErrorsMap.getDisplayErrorMessageBySyncErrorMessage(model.getSyncServerErrorMessage());
                }
                configHolder(model.getPeriod(), Html.fromHtml(error), R.drawable.ic_error, R.color.color_red, form);
                break;
            case RnRFormViewModel.TYPE_SYNCED_HISTORICAL:
                configHolder(model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done_green, INT_UNSET, form);
                btnView.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                btnView.setOnClickListener(new BtnViewClickListener(model));
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

        if (ivDelete != null && itemClickListener != null) {
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

    private class BtnViewClickListener implements View.OnClickListener {

        private final RnRFormViewModel model;

        public BtnViewClickListener(final RnRFormViewModel model) {
            this.model = model;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.clickBtnView(model);
            }
        }
    }
}
