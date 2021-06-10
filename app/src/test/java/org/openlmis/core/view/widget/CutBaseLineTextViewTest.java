/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.view.widget;

import android.graphics.Canvas;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class CutBaseLineTextViewTest {

  private CutBaseLineTextView cutBaseLineTextView;

  @Before
  public void setUp() throws Exception {
    cutBaseLineTextView = new CutBaseLineTextView(LMISTestApp.getContext());
  }

  @Test
  public void translateShouldInvokeCorrectTimes() {
    // given
    final Canvas mockCanvas = Mockito.mock(Canvas.class);

    // when
    cutBaseLineTextView.onDraw(mockCanvas);

    // then
    Mockito.verify(mockCanvas, Mockito.times(1))
        .translate(0F, (float) cutBaseLineTextView.getPaint().getFontMetricsInt().descent);
  }
}