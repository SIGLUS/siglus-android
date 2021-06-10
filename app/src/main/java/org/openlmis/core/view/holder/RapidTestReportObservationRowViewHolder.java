package org.openlmis.core.view.holder;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import org.openlmis.core.R;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import roboguice.inject.InjectView;


public class RapidTestReportObservationRowViewHolder extends BaseViewHolder {

  @InjectView(R.id.rv_observation_content)
  EditText observationContent;

  private RapidTestReportViewModel rapidTestReportViewModel;

  private RapidTestReportObservationRowViewHolder.EditTextWatcher textWatcher;

  public RapidTestReportObservationRowViewHolder(View itemView) {
    super(itemView);
  }


  public void populate(RapidTestReportViewModel viewModel) {
    Boolean editable = viewModel.isEditable();
    observationContent.setEnabled(editable);
    observationContent.setFocusableInTouchMode(editable);
    observationContent.setText(viewModel.getObservation());
    rapidTestReportViewModel = viewModel;
    if (textWatcher != null) {
      observationContent.removeTextChangedListener(textWatcher);
    }
    if (editable) {
      textWatcher =
          new RapidTestReportObservationRowViewHolder.EditTextWatcher(observationContent);
      observationContent.addTextChangedListener(textWatcher);
    }
  }


  class EditTextWatcher extends SimpleTextWatcher {

    private final EditText editText;

    public EditTextWatcher(EditText editText) {
      this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable etText) {
      rapidTestReportViewModel.setObservation(etText.toString());
    }
  }

}
