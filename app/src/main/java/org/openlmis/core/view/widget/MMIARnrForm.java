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
package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.view.viewmodel.RapidTestFormItemViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.text.ParseException;
import java.util.List;

import lombok.Getter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class MMIARnrForm extends LinearLayout {
    private ViewGroup leftViewGroup;

    public List<RnrFormItem> itemFormList;

    @Getter
    private ViewGroup rightViewGroup;
    private LayoutInflater layoutInflater;

    @Getter
    private RnrFormHorizontalScrollView rnrItemsHorizontalScrollView;

    @Getter
    private View leftHeaderView;
    @Getter
    private ViewGroup rightHeaderView;

    public MMIARnrForm(Context context) {
        super(context);
        init(context);
    }

    public MMIARnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this, true);
        rnrItemsHorizontalScrollView = (RnrFormHorizontalScrollView) container.findViewById(R.id.vg_right_scrollview);
        leftViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list_product_name);
        rightViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list);
    }

    public void initView(List<RnrFormItem> list) {
        addHeaderView();
        addItemView(list);
    }

    private void addHeaderView() {
        leftHeaderView = addLeftHeaderView();
        rightHeaderView = addRightHeaderView();
        setItemSize(leftHeaderView, rightHeaderView);

        setMarginForFreezeHeader();
    }

    private void setMarginForFreezeHeader() {
        post(new Runnable() {
            @Override
            public void run() {
                final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
                marginLayoutParams.topMargin = rightHeaderView.getLayoutParams().height;
                setLayoutParams(marginLayoutParams);
            }
        });
    }

    private void addItemView(List<RnrFormItem> rnrFormItemList) {
        itemFormList = rnrFormItemList;

        // Adult View
        addViewByMedicineType(filterRnrFormItem(itemFormList, Product.MEDICINE_TYPE_ADULT));
        addDividerView(Product.MEDICINE_TYPE_ADULT);
        addDividerView(Product.MEDICINE_TYPE_ADULT);

        // Children View
        addViewByMedicineType(filterRnrFormItem(itemFormList, Product.MEDICINE_TYPE_CHILDREN));
        addDividerView(Product.MEDICINE_TYPE_CHILDREN);

        // Solution View
        addViewByMedicineType(filterRnrFormItem(itemFormList, Product.MEDICINE_TYPE_SOLUTION));
        addDividerView(Product.MEDICINE_TYPE_SOLUTION);
    }


    private List<RnrFormItem> filterRnrFormItem(List<RnrFormItem> rnrFormItemList, final String category) {
        return from(rnrFormItemList).filter(new Predicate<RnrFormItem>() {
            @Override
            public boolean apply(RnrFormItem rnrFormItem) {
                return category.equals(rnrFormItem.getCategory());
            }
        }).toList();
    }

    private void addViewByMedicineType(List<RnrFormItem> categoriedFormItems) {
        for (RnrFormItem item : categoriedFormItems) {
            addRnrFormItemView(item.getCategory(), item);
        }
    }

    private void addRnrFormItemView(String medicineTypeName, RnrFormItem item) {
        View leftView = addLeftView(item, false, medicineTypeName);
        ViewGroup rightView = addRightView(item, false);
        setItemSize(leftView, rightView);
    }

    private void addDividerView(String medicineType) {
        View leftView = inflaterLeftView();
        leftViewGroup.addView(leftView);
        setLeftViewColor(medicineType, leftView);
        ViewGroup rightView = inflateRightView();
        rightViewGroup.addView(rightView);
        setItemSize(leftView, rightView);
    }

    private View inflaterLeftView() {
        return layoutInflater.inflate(R.layout.item_rnr_from_product_name, this, false);
    }

    private ViewGroup inflateRightView() {
        return (ViewGroup) layoutInflater.inflate(R.layout.item_rnr_from, this, false);
    }

    public void setItemSize(final View leftView, final ViewGroup rightView) {
        post(new Runnable() {
            @Override
            public void run() {
                setRightItemWidth(rightView);
                ViewUtil.syncViewHeight(leftView, rightView);
            }
        });
    }

    private void setRightItemWidth(final ViewGroup rightView) {
        int rightWidth = rnrItemsHorizontalScrollView.getWidth();
        int rightViewGroupWidth = rightViewGroup.getWidth();

        if (rightViewGroupWidth < rightWidth) {
            int childCount = rightView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                rightView.getChildAt(i).getLayoutParams().width = getRightViewWidth(rightWidth, childCount);
            }
            rightView.getChildAt(0).getLayoutParams().width = getRightViewWidth(rightWidth, childCount) + getRightViewRemainderWidth(rightWidth, childCount);
        }
    }

    public View addLeftHeaderView() {
        return addLeftView(null, true, null);
    }

    private View addLeftView(RnrFormItem item, boolean isHeaderView, String medicineType) {
        View view = inflaterLeftView();
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        if (isHeaderView) {
            tvPrimaryName.setText(R.string.label_rnrfrom_left_header);
            tvPrimaryName.setGravity(Gravity.CENTER);
            view.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            Product product = item.getProduct();
            tvPrimaryName.setText(product.getPrimaryName());
            setLeftViewColor(medicineType, view);
            leftViewGroup.addView(view);
        }

        return view;
    }

    private void setLeftViewColor(String medicineType, View view) {
        switch (medicineType) {
            case Product.MEDICINE_TYPE_ADULT:
                view.setBackgroundResource(R.color.color_green_light);
                break;
            case Product.MEDICINE_TYPE_CHILDREN:
                view.setBackgroundResource(R.color.color_regime_baby);
                break;
            case Product.MEDICINE_TYPE_SOLUTION:
                view.setBackgroundResource(R.color.color_regime_other);
                break;
            default:
                break;
        }
    }


    public ViewGroup addRightHeaderView() {
        return addRightView(null, true);
    }

    private ViewGroup addRightView(RnrFormItem item, boolean isHeaderView) {
        ViewGroup inflate = inflateRightView();

        TextView tvIssuedUnit = (TextView) inflate.findViewById(R.id.tv_issued_unit);
        TextView tvInitialAmount = (TextView) inflate.findViewById(R.id.tv_initial_amount);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        EditText etIssued = (EditText) inflate.findViewById(R.id.et_issued);
        EditText etAdjustment = (EditText) inflate.findViewById(R.id.et_adjustment);
        EditText etInventory = (EditText) inflate.findViewById(R.id.et_inventory);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_validate);

        if (isHeaderView) {
            tvIssuedUnit.setText(R.string.label_issued_unit);
            tvInitialAmount.setText(R.string.label_initial_amount);
            tvReceived.setText(R.string.label_received_mmia);
            etIssued.setText(R.string.label_issued_mmia);
            etIssued.setEnabled(false);
            etAdjustment.setText(R.string.label_adjustment);
            etAdjustment.setEnabled(false);
            etInventory.setText(R.string.label_inventory);
            etInventory.setEnabled(false);
            tvValidate.setText(R.string.label_validate);

            inflate.setBackgroundResource(R.color.color_mmia_info_name);

        } else {
            tvIssuedUnit.setText(item.getProduct().getStrength());

            boolean isArchived = item.getProduct().isArchived();

            tvInitialAmount.setText(String.valueOf(isArchived ? 0 : item.getInitialAmount()));
            tvReceived.setText(String.valueOf(isArchived ? 0 : item.getReceived()));
            etIssued.setText(getValue(isArchived, item.getIssued()));
            etAdjustment.setText(getValue(isArchived, item.getAdjustment()));
            etAdjustment.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_SIGNED);
            etInventory.setText(getValue(isArchived, item.getInventory()));
            setTextWatcher(etIssued, etAdjustment, etInventory, item);

            rightViewGroup.addView(inflate);

            try {
                if (!(TextUtils.isEmpty(item.getValidate()) || isArchived)) {
                    tvValidate.setText(DateUtil.convertDate(item.getValidate(), "dd/MM/yyyy", "MMM yyyy"));
                }
            } catch (ParseException e) {
                new LMISException(e).reportToFabric();
            }
        }
        return inflate;
    }

    private void setTextWatcher(EditText etIssued, EditText etAdjustment, EditText etInventory, RnrFormItem item) {
        EditTextWatcher twIssued = new EditTextWatcher(item, etIssued);
        EditTextWatcher twAdjustment= new EditTextWatcher(item, etAdjustment);
        EditTextWatcher twInventory = new EditTextWatcher(item, etInventory);

        etIssued.removeTextChangedListener(twIssued);
        etAdjustment.removeTextChangedListener(twAdjustment);
        etInventory.removeTextChangedListener(twInventory);

        etIssued.addTextChangedListener(twIssued);
        etAdjustment.addTextChangedListener(twAdjustment);
        etInventory.addTextChangedListener(twInventory);

    }

    private String getValue(Boolean isArchived, Long vaule) {
        if (isArchived) return String.valueOf(0);
        return vaule == null ? "": String.valueOf(vaule.longValue());

    }

    private int getRightViewWidth(int rightWidth, int childCount) {
        return (rightWidth - (childCount - 1) * getDividerWidth()) / childCount;
    }

    private int getRightViewRemainderWidth(int rightWidth, int childCount) {
        return (rightWidth - (childCount - 1) * getDividerWidth()) % childCount;
    }

    private int getDividerWidth() {
        return (int) getResources().getDimension(R.dimen.divider);
    }

    class EditTextWatcher extends SingleTextWatcher {
        private final RnrFormItem item;
        private final EditText editText;

        public EditTextWatcher(RnrFormItem item, EditText editText) {
            this.item = item;
            this.editText = editText;

        }

        @Override
        public void afterTextChanged(Editable etText) {
            switch (editText.getId()) {
                case  R.id.et_inventory:
                    item.setInventory(Long.valueOf(etText.toString()));
                    break;
                case  R.id.et_issued:
                    item.setIssued(Long.valueOf(etText.toString()));
                    break;
                case  R.id.et_adjustment:
                    try {
                        item.setAdjustment(Long.valueOf(etText.toString()));
                    } catch (NumberFormatException e) {
                        item.setAdjustment(null);
                    }
                    break;
            }

        }
    }

}
