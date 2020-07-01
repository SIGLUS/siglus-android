package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.ListViewUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.adapter.RequisitionProductAdapter;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

import static org.openlmis.core.view.widget.DoubleListScrollListener.scrollInSync;

public class ViaRequisitionBodyView extends FrameLayout {
    @InjectView(R.id.requisition_form_list_view)
    ListView requisitionFormList;

    @InjectView(R.id.product_name_list_view)
    ListView requisitionProductList;

    @InjectView(R.id.requisition_header_right)
    View bodyHeaderView;

    @InjectView(R.id.requisition_header_left)
    View productHeaderView;

    @InjectView(R.id.form_layout)
    HorizontalScrollView formLayout;

    @InjectView(R.id.tv_label_request)
    TextView headerRequestAmount;

    @InjectView(R.id.tv_label_approve)
    TextView headerApproveAmount;

    private VIARequisitionPresenter presenter;

    @Inject
    Context context;

    private RequisitionFormAdapter requisitionFormAdapter;

    private RequisitionProductAdapter requisitionProductAdapter;

    public ViaRequisitionBodyView(Context context) {
        super(context);
        init(context);
    }

    public ViaRequisitionBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.via_requisition_body, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void showListInputError(final int position) {
        requisitionFormList.setSelection(position);
        requisitionProductList.setSelection(position);
        requisitionFormList.post(() -> {
            View childAt = ListViewUtil.getViewByPosition(position, requisitionFormList);
            EditText requestAmount = (EditText) childAt.findViewById(R.id.et_request_amount);
            EditText approvedAmount = (EditText) childAt.findViewById(R.id.et_approved_amount);
            if (requestAmount.isEnabled()) {
                requestAmount.requestFocus();
                requestAmount.setError(getResources().getString(R.string.hint_error_input));
            } else {
                approvedAmount.requestFocus();
                approvedAmount.setError(getResources().getString(R.string.hint_error_input));
            }
        });
    }

    public void initUI(VIARequisitionPresenter presenter) {
        this.presenter = presenter;
        this.requisitionFormAdapter = new RequisitionFormAdapter(context, presenter);
        this.requisitionProductAdapter = new RequisitionProductAdapter(context, presenter);
        requisitionFormList.setAdapter(requisitionFormAdapter);
        requisitionProductList.setAdapter(requisitionProductAdapter);
        requisitionProductList.post(() -> productHeaderView.getLayoutParams().height = bodyHeaderView.getHeight());
        scrollInSync(requisitionFormList, requisitionProductList);
    }

    public void autoScrollLeftToRight() {
        if (!presenter.isHistoryForm()) {
            formLayout.post(() -> formLayout.fullScroll(FOCUS_RIGHT));
        }
    }

    public void refresh(RnRForm rnRForm) {
        refreshProductNameList();
        refreshFormList(rnRForm.getStatus());
    }

    public void setEditable(boolean isRnrFormMissed) {
        if (isRnrFormMissed) {
            requisitionFormList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        } else {
            requisitionFormList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        }
    }

    public void refreshProductNameList() {
        requisitionProductAdapter.notifyDataSetChanged();
    }

    public void refreshFormList(RnRForm.STATUS status) {
        requisitionFormAdapter.updateStatus(status);
    }

    public void highLightApprovedAmount() {
        headerRequestAmount.setBackgroundResource(android.R.color.transparent);
        headerRequestAmount.setTextColor(getResources().getColor(R.color.color_text_primary));
        headerApproveAmount.setBackgroundResource(R.color.color_accent);
        headerApproveAmount.setTextColor(getResources().getColor(R.color.color_white));

        refreshFormList(RnRForm.STATUS.SUBMITTED);
    }

    public void highLightRequestAmount() {
        headerRequestAmount.setBackgroundResource(R.color.color_accent);
        headerRequestAmount.setTextColor(getResources().getColor(R.color.color_white));
        headerApproveAmount.setBackgroundResource(android.R.color.transparent);
        headerApproveAmount.setTextColor(getResources().getColor(R.color.color_text_primary));

        refreshFormList(RnRForm.STATUS.DRAFT);
    }

    public void setHideImmOnTouchListener() {
        formLayout.setOnTouchListener((v, event) -> {
            if (context instanceof BaseActivity) {
                ((BaseActivity) context).hideImm();
            }
            return false;
        });

    }
}