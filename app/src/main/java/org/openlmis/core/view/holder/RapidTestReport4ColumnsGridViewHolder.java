package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode;
import roboguice.inject.InjectView;

public class RapidTestReport4ColumnsGridViewHolder extends RapidTestReportGridViewHolder {

  @InjectView(R.id.et_positive_hiv_rapid_test_report_grid)
  EditText etPositiveHiv;

  @InjectView(R.id.et_positive_hiv_rapid_test_report_grid_total)
  TextView etPositiveHivTotal;

  @InjectView(R.id.et_positive_syphilis_rapid_test_report_grid)
  EditText etPositiveSyphilis;

  @InjectView(R.id.et_positive_syphilis_rapid_test_report_grid_total)
  TextView etPositiveSyphilisTotal;

  public RapidTestReport4ColumnsGridViewHolder(View itemView) {
    super(itemView);
  }

  @Override
  public void populateData(RapidTestFormGridViewModel viewModel) {
    (editable ? etConsume : etConsumeTotal).setText(viewModel.getConsumptionValue());
    (editable ? etPositiveHiv : etPositiveHivTotal).setText(viewModel.getPositiveHivValue());
    (editable ? etPositiveSyphilis : etPositiveSyphilisTotal)
        .setText(viewModel.getPositiveSyphilisValue());
    (editable ? etUnjustified : etUnjustifiedTotal).setText(viewModel.getUnjustifiedValue());
  }

  @Override
  public void setEditable(Boolean editable) {
    if (Boolean.TRUE.equals(editable)) {
      setEditable(etConsume);
      setEditable(etPositiveHiv);
      setEditable(etPositiveSyphilis);
      setEditable(etUnjustified);
    }
  }

  private void setEditable(EditText editText) {
    editText.setFocusable(true);
    editText.setOnFocusChangeListener(getOnFocusChangeListener());
  }

  @Override
  void setTextWatcher() {
    if (editable) {
      setTextWatcherForET(etConsume);
      setTextWatcherForET(etPositiveHiv);
      setTextWatcherForET(etPositiveSyphilis);
      setTextWatcherForET(etUnjustified);
    }
  }

  private void setTextWatcherForET(EditText editText) {
    TextWatcher textWatcherConsume = new TextWatcher(editText);
    editText.addTextChangedListener(textWatcherConsume);
  }

  @Override
  void updateAlert() {
    // TODO: confirm the alert rule
  }

  @Override
  void setInvalidInput() {
    RapidTestGridColumnCode invalidColumn = viewModel.getInvalidColumn();
    if (invalidColumn != null) {
      switch (invalidColumn) {
        case CONSUMPTION:
          showError(etConsume);
          break;
        case POSITIVE_HIV:
          showError(etPositiveHiv);
          break;
        case POSITIVE_SYPHILIS:
          showError(etPositiveSyphilis);
          break;
        case UNJUSTIFIED:
          showError(etUnjustified);
          break;
        default:
          break;
      }
    }
  }

  private void showError(EditText etConsume) {
    etConsume.setError(getString(R.string.hint_error_input));
  }

  @Override
  void clearError() {
    clearErrorForET(etConsume);
    clearErrorForET(etPositiveHiv);
    clearErrorForET(etPositiveSyphilis);
    clearErrorForET(etUnjustified);
  }

  private void clearErrorForET(EditText editText) {
    editText.setError(null);
  }
}