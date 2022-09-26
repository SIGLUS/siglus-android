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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.constant.ReportConstants;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.robolectric.RuntimeEnvironment;

@RunWith(LMISTestRunner.class)
public class MMTBThreeLineListTest {

  private MMTBPatientThreeLineList mmtbThreeLineList;
  private ArrayList<RegimenItemThreeLines> dataList;

  @Before
  public void setUp() {
    mmtbThreeLineList = new MMTBPatientThreeLineList(RuntimeEnvironment.application);
    mmtbThreeLineList.setLayoutParams(new ViewGroup.MarginLayoutParams(100, 100));
    dataList = new ArrayList<>();
    RegimenItemThreeLines line1 = new RegimenItemThreeLines();
    RegimenItemThreeLines line2 = new RegimenItemThreeLines();
    RegimenItemThreeLines line3 = new RegimenItemThreeLines();
    Product product = new Product();
    product.setId(1L);
    product.setPrimaryName("product");
    product.setCode("08S17");
    RnRForm rnRForm = RnRForm.init(Program.builder().programCode(Program.MMTB_CODE).build(), new Date());
    line1.setRegimeTypes(ReportConstants.KEY_MMTB_THREE_LINE_1);
    line1.setForm(rnRForm);
    line2.setRegimeTypes(ReportConstants.KEY_MMTB_THREE_LINE_2);
    line2.setForm(rnRForm);
    line3.setRegimeTypes(ReportConstants.KEY_MMTB_THREE_LINE_3);
    line3.setForm(rnRForm);
    dataList.add(line1);
    dataList.add(line2);
    dataList.add(line3);
  }

  @Test
  public void shouldShowCorrectData() {
    // when
    mmtbThreeLineList.setData(dataList);

    // then
    ViewGroup container = mmtbThreeLineList.findViewById(R.id.mmtb_requisition_age_range_container);
    String name1 = ((TextView) container.getChildAt(0).findViewById(R.id.tv_title)).getText().toString();
    String name2 = ((TextView) container.getChildAt(1).findViewById(R.id.tv_title)).getText().toString();
    String name3 = ((TextView) container.getChildAt(2).findViewById(R.id.tv_title)).getText().toString();
    assertThat(name1, is(LMISTestApp.getContext().getString(R.string.mmtb_three_line_1)));
    assertThat(name2, is(LMISTestApp.getContext().getString(R.string.mmtb_three_line_2)));
    assertThat(name3, is(LMISTestApp.getContext().getString(R.string.mmtb_three_line_3)));
  }

  @Test
  public void shouldNotCompleteWhenInputNothing() {
    // when
    mmtbThreeLineList.setData(dataList);

    // then
    assertThat(mmtbThreeLineList.isCompleted(), is(false));
  }

  @Test
  public void shouldCompleteWhenFillAllEditText() {
    // when
    mmtbThreeLineList.setData(dataList);

    // then
    ViewGroup container = mmtbThreeLineList.findViewById(R.id.mmtb_requisition_age_range_container);
    ((EditText) container.getChildAt(0).findViewById(R.id.therapeutic_total)).setText("1");
    ((EditText) container.getChildAt(0).findViewById(R.id.therapeutic_pharmacy)).setText("1");
    ((EditText) container.getChildAt(1).findViewById(R.id.therapeutic_total)).setText("1");
    ((EditText) container.getChildAt(1).findViewById(R.id.therapeutic_pharmacy)).setText("1");
    ((EditText) container.getChildAt(2).findViewById(R.id.therapeutic_total)).setText("1");
    ((EditText) container.getChildAt(2).findViewById(R.id.therapeutic_pharmacy)).setText("1");
    assertThat(mmtbThreeLineList.isCompleted(), is(true));
  }
}
