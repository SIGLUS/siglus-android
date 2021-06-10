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
