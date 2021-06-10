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

package org.openlmis.core.view.fragment.builders;

import com.google.inject.Inject;
import org.openlmis.core.view.fragment.WarningDialogFragment;

public class WarningDialogFragmentBuilder {

  @Inject
  public WarningDialogFragmentBuilder() {
  }

  public WarningDialogFragment build(WarningDialogFragment.DialogDelegate delegate,
      int message, int positiveMessageButton,
      int negativeMessageButton) {

    WarningDialogFragment wipeDataDialog = WarningDialogFragment.newInstance(
        message, positiveMessageButton, negativeMessageButton);
    wipeDataDialog.setDelegate(delegate);
    return wipeDataDialog;
  }

  public WarningDialogFragment build(WarningDialogFragment.DialogDelegate delegate,
      String message, String positiveMessageButton,
      String negativeMessageButton) {

    WarningDialogFragment warningDataDialog = WarningDialogFragment.newInstanceForDeleteProduct(
        message, positiveMessageButton, negativeMessageButton);
    warningDataDialog.setDelegate(delegate);
    return warningDataDialog;
  }

}
