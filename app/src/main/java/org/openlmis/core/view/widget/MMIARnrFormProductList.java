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
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
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
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ViewUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class MMIARnrFormProductList extends LinearLayout {
    private Context context;
    private ViewGroup leftViewGroup;

    public List<RnrFormItem> itemFormList;

    @Getter
    private ViewGroup rightViewGroup;
    private LayoutInflater layoutInflater;

    @Getter
    private RnrFormHorizontalScrollView rnrItemsHorizontalScrollView;
    private List<Pair<EditText, EditTextWatcher>> editTexts = new ArrayList<Pair<EditText, EditTextWatcher>>();

    @Getter
    private View leftHeaderView;
    @Getter
    private ViewGroup rightHeaderView;

    private boolean dataWithOldFormat = false;

    public MMIARnrFormProductList(Context context) {
        super(context);
        init(context);
    }

    public MMIARnrFormProductList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (Pair<EditText, EditTextWatcher> editText : editTexts) {
            editText.first.removeTextChangedListener(editText.second);
        }
    }

    private void init(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this, true);
        rnrItemsHorizontalScrollView = (RnrFormHorizontalScrollView) container.findViewById(R.id.vg_right_scrollview);
        leftViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list_product_name);
        rightViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list);
    }

    public void initView(List<RnrFormItem> list, boolean shouldOldData) {
        this.dataWithOldFormat = shouldOldData;
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
        post(() -> {
            final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
            marginLayoutParams.topMargin = rightHeaderView.getLayoutParams().height;
            setLayoutParams(marginLayoutParams);
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

        //fill others type items
        fillOtherTypeItem();

    }

    public void fillOtherTypeItem() {
        List<RnrFormItem> otherItems = filterRnrFormItem(itemFormList, Product.MEDICINE_TYPE_OTHER);
        for (RnrFormItem rnrFormItem : otherItems) {
            rnrFormItem.setIssued((long) 0);
            rnrFormItem.setAdjustment((long) 0);
            rnrFormItem.setInventory((long) 0);
        }
    }

    public boolean isCompleted() {
        for (Pair<EditText, EditTextWatcher> editText : editTexts) {
            if (TextUtils.isEmpty(editText.first.getText().toString()) || !isValidate(editText.first)) {
                editText.first.setError(context.getString(R.string.hint_error_input));
                editText.first.requestFocus();
                return false;
            }
        }
        return true;
    }

    private boolean isValidate(EditText editText) {
        if (editText.getId() != R.id.et_adjustment) return true;
        Long text;
        try {
            text = Long.valueOf(editText.getText().toString());
        } catch (NumberFormatException e) {
            text = null;
        }
        return text != null;
    }


    private List<RnrFormItem> filterRnrFormItem(List<RnrFormItem> rnrFormItemList, final String category) {
        return from(rnrFormItemList).filter(rnrFormItem -> category.equals(rnrFormItem.getCategory())).toList();
    }

    private void addViewByMedicineType(List<RnrFormItem> categoryFormItems) {
        for (RnrFormItem item : categoryFormItems) {
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
        if (dataWithOldFormat) {
            leftView.findViewById(R.id.tv_product_code_header).setVisibility(View.GONE);
        }
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
        post(() -> {
            setRightItemWidth(rightView);
            ViewUtil.syncViewHeight(leftView, rightView);
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
        TextView tvProductCodeHeader = (TextView) view.findViewById(R.id.tv_product_code_header);
        if (isHeaderView) {
            tvPrimaryName.setText(R.string.label_rnrfrom_left_header);
            tvPrimaryName.setGravity(Gravity.CENTER);
            if (dataWithOldFormat) {
                tvProductCodeHeader.setVisibility(GONE);
            } else {
                tvProductCodeHeader.setText(R.string.label_product_codes);
            }
        } else {
            Product product = item.getProduct();
            tvPrimaryName.setText(product.getPrimaryName());
            if (dataWithOldFormat) {
                tvProductCodeHeader.setVisibility(GONE);
            } else {
                tvProductCodeHeader.setText(product.getCode());
                tvProductCodeHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                        new BitmapDrawable(getResources(), DateUtil.createBarcode(product.getCode())),
                        null, null);
            }
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
            setHeaderView(inflate, tvIssuedUnit, tvInitialAmount, tvReceived, etIssued, etAdjustment, etInventory, tvValidate);

        } else {
            tvIssuedUnit.setText(item.getProduct().getStrength());
            boolean isArchived = item.getProduct().isArchived();
            tvInitialAmount.setText(getValue(isArchived, item.getInitialAmount()));
            tvReceived.setText(getValue(isArchived, item.getReceived()));
            editTexts.add(configEditText(item, etIssued, getValue(isArchived, item.getIssued())));
            editTexts.add(configEditText(item, etAdjustment, getValue(isArchived, item.getAdjustment())));
            editTexts.add(configEditText(item, etInventory, getValue(isArchived, item.getInventory())));
            rightViewGroup.addView(inflate);

            try {
                if (!(TextUtils.isEmpty(item.getValidate()) || isArchived)) {
                    tvValidate.setText(DateUtil.convertDate(item.getValidate(), DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                }
            } catch (ParseException e) {
                new LMISException(e, "MMIARnrForm.addRightView").reportToFabric();
            }
        }
        return inflate;
    }

    private void setHeaderView(ViewGroup inflate,
                               TextView tvIssuedUnit,
                               TextView tvInitialAmount,
                               TextView tvReceived,
                               EditText etIssued,
                               EditText etAdjustment,
                               EditText etInventory,
                               TextView tvValidate) {
        tvIssuedUnit.setText(R.string.label_issued_unit);
        tvInitialAmount.setText(R.string.label_initial_amount);
        tvReceived.setText(R.string.label_received_mmia);
        etIssued.setText(R.string.label_issued_mmia);
        etAdjustment.setText(R.string.label_adjustment);
        etInventory.setText(R.string.label_inventory);
        tvValidate.setText(R.string.label_validate);
        enableEditText(false, etIssued, etAdjustment, etInventory);

        inflate.setBackgroundResource(R.color.color_mmia_info_name);
    }

    private Pair<EditText, EditTextWatcher> configEditText(RnrFormItem item, EditText text, String value) {
        text.setText(value);
        text.setEnabled(true);
        EditTextWatcher textWatcher = new EditTextWatcher(item, text);
        text.addTextChangedListener(textWatcher);
        return new Pair<>(text, textWatcher);
    }

    private void enableEditText(Boolean enable, EditText etIssued, EditText etAdjustment, EditText etInventory) {
        etIssued.setEnabled(enable);
        etAdjustment.setEnabled(enable);
        etInventory.setEnabled(enable);
    }


    private String getValue(Boolean isArchived, Long value) {
        if (isArchived) return String.valueOf(0);
        return value == null ? "" : String.valueOf(value.longValue());

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

    class EditTextWatcher extends SimpleTextWatcher {
        private final RnrFormItem item;
        private final EditText editText;

        public EditTextWatcher(RnrFormItem item, EditText editText) {
            this.item = item;
            this.editText = editText;

        }

        @Override
        public void afterTextChanged(Editable etText) {
            switch (editText.getId()) {
                case R.id.et_inventory:
                    item.setInventory(getEditValue(etText));
                    break;
                case R.id.et_issued:
                    item.setIssued(getEditValue(etText));
                    break;
                case R.id.et_adjustment:
                    item.setAdjustment(getEditValue(etText));
                    break;
            }

        }

        private Long getEditValue(Editable etText) {
            Long editText;
            try {
                editText = Long.valueOf(etText.toString());
            } catch (NumberFormatException e) {
                editText = null;
            }
            return editText;
        }
    }

}
