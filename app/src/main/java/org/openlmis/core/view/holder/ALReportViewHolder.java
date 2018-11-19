package org.openlmis.core.view.holder;
import android.text.Editable;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import org.openlmis.core.R;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.viewmodel.ALGridViewModel;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.inject.InjectView;

public class ALReportViewHolder extends BaseViewHolder {

    private ALReportItemViewModel viewModel;
    private QuantityChangeListener quantityChangeListener;
    @InjectView(R.id.one_treatment)
    EditText oneTreatment;
    @InjectView(R.id.two_treatment)
    EditText twoTreatment;
    @InjectView(R.id.three_treatment)
    EditText threeTreatment;
    @InjectView(R.id.four_treatment)
    EditText fourTreatment;
    @InjectView(R.id.one_Stock)
    EditText oneStock;
    @InjectView(R.id.two_Stock)
    EditText twoStock;
    @InjectView(R.id.three_stock)
    EditText threeStock;
    @InjectView(R.id.four_stock)
    EditText fourStock;

    private List<Pair<EditText, EditTextWatcher>> editPairWatcher = new ArrayList<>();
    private List<EditText> editTexts = new ArrayList<>();


    public ALReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final ALReportItemViewModel alReportViewModel, QuantityChangeListener changeListener) {
        this.viewModel = alReportViewModel;
        removeTextWatcher();
        setEditTextValue();
        if (viewModel.getItemType() != ALReportViewModel.ALItemType.Total) {
            this.quantityChangeListener = changeListener;
            configureEditTextWatch();
            checkTips();
        } else {
            for (EditText editText : editTexts) {
                editText.setEnabled(false);
                editText.setError(null);
            }
        }

    }

    public void setEditTextValue() {
        oneTreatment.setText(getValue(viewModel.getGridOne().getTreatmentsValue()));
        oneStock.setText(getValue(viewModel.getGridOne().getExistentStockValue()));
        twoTreatment.setText(getValue(viewModel.getGridTwo().getTreatmentsValue()));
        twoStock.setText(getValue(viewModel.getGridTwo().getExistentStockValue()));
        threeTreatment.setText(getValue(viewModel.getGridThree().getTreatmentsValue()));
        threeStock.setText(getValue(viewModel.getGridThree().getExistentStockValue()));
        fourTreatment.setText(getValue(viewModel.getGridFour().getTreatmentsValue()));
        fourStock.setText(getValue(viewModel.getGridFour().getExistentStockValue()));
        editTexts = Arrays.asList(oneTreatment, oneStock, twoTreatment, twoStock,
                threeTreatment,threeStock, fourTreatment, fourStock);
    }

    public void configureEditTextWatch() {
        for (EditText editText: editTexts) {
            editText.setEnabled(true);
            editText.setError(null);
            ALReportViewHolder.EditTextWatcher textWatcher = new ALReportViewHolder.EditTextWatcher(editText);
            editText.addTextChangedListener(textWatcher);
            editPairWatcher.add(new Pair(editText, textWatcher));
        }
    }

    public void checkTips() {
        if (viewModel.showCheckTip == false) return;
        for (EditText editText: editTexts) {
            if (editText.getText().length() == 0) {
                editText.setError(context.getString(R.string.hint_error_input));
                return;
            }
        }
    }

    private String getValue(Long vaule) {
        return vaule == null ? "": String.valueOf(vaule.longValue());
    }

    class EditTextWatcher extends SimpleTextWatcher {
        private final EditText editText;

        public EditTextWatcher(EditText editText) {
            this.editText = editText;

        }

        @Override
        public void afterTextChanged(Editable etText) {
            ALGridViewModel gridViewModel = viewModel.getGridOne();
            ALGridViewModel.ALGridColumnCode gridColumnCode = ALGridViewModel.ALGridColumnCode.treatment;
            switch (editText.getId()) {
                case  R.id.one_treatment:
                    gridViewModel = viewModel.getGridOne();
                    break;
                case  R.id.one_Stock:
                    gridViewModel = viewModel.getGridOne();
                    gridColumnCode = ALGridViewModel.ALGridColumnCode.existentStock;
                    break;
                case  R.id.two_treatment:
                    gridViewModel = viewModel.getGridTwo();
                    break;
                case  R.id.two_Stock:
                    gridViewModel = viewModel.getGridTwo();
                    gridColumnCode = ALGridViewModel.ALGridColumnCode.existentStock;
                    break;
                case  R.id.three_treatment:
                    gridViewModel = viewModel.getGridThree();
                    break;
                case  R.id.three_stock:
                    gridViewModel = viewModel.getGridThree();
                    gridColumnCode = ALGridViewModel.ALGridColumnCode.existentStock;
                    break;
                case  R.id.four_treatment:
                    gridViewModel = viewModel.getGridFour();
                    break;
                case  R.id.four_stock:
                    gridViewModel = viewModel.getGridFour();
                    gridColumnCode = ALGridViewModel.ALGridColumnCode.existentStock;
                    break;
            }
            if(gridColumnCode == ALGridViewModel.ALGridColumnCode.treatment) {
                gridViewModel.setTreatmentsValue(getEditValue(etText));
            } else {
                gridViewModel.setExistentStockValue(getEditValue(etText));
            }
            quantityChangeListener.updateTotal(gridViewModel.getColumnCode(), gridColumnCode);
        }

        private Long getEditValue(Editable etText) {
            Long editText;
            try { editText = Long.valueOf(etText.toString());
            } catch (NumberFormatException e) {
                editText = null;
            }
            return editText;
        }
    }

    private  void removeTextWatcher() {
        for (Pair<EditText, EditTextWatcher> editText: editPairWatcher) {
            if (editText.first != null) {
                editText.first.removeTextChangedListener(editText.second);
            }
        }
        editPairWatcher.clear();
    }

    public interface QuantityChangeListener {
        void updateTotal(ALGridViewModel.ALColumnCode columnCode, ALGridViewModel.ALGridColumnCode gridColumnCode);
    }
}
