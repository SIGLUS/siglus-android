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
package org.openlmis.core.view.fragment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Dialog;
import androidx.appcompat.app.AlertDialog;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.robolectric.util.FragmentTestUtil;

@RunWith(LMISTestRunner.class)
public class SimpleDialogFragmentTest {

  // TODO: robolectric.android.controller.FragmentController with RoboContext
  @Ignore
  @Test
  public void shouldSetCallBack() {
    SimpleDialogFragment fragment = SimpleDialogFragment.newInstance(
        "title",
        "message",
        "btn_positive",
        "btn_negative",
        "onBackPressed");

    SimpleDialogFragment.MsgDialogCallBack dialogCallBack = mock(
        SimpleDialogFragment.MsgDialogCallBack.class);
    fragment.setCallBackListener(dialogCallBack);
    FragmentTestUtil.startFragment(fragment);

    Dialog dialog = fragment.getDialog();
    (((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)).performClick();
    verify(dialogCallBack).positiveClick(anyString());
  }
}