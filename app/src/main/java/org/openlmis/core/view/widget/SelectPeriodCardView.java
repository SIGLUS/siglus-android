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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;

public class SelectPeriodCardView extends CardView implements Checkable {

  private boolean mChecked;

  private View inventoryContainer;
  private View horizontalLine;
  private TextView inventoryDateTime;
  private TextView inventoryDateDay;
  private TextView inventoryDateMonth;
  private TextView inventoryDateWeek;
  private ImageView checkmarkIcon;

  public SelectPeriodCardView(Context context) {
    super(context);
    init();
  }

  public SelectPeriodCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    inflate(getContext(), R.layout.view_card_checkable, this);
    setRadius(getResources().getDimension(R.dimen.cardview_radius));
    inventoryDateTime = findViewById(R.id.tv_inventory_date_time);
    inventoryDateDay = findViewById(R.id.tv_inventory_date_day);
    inventoryDateMonth = findViewById(R.id.tv_inventory_date_month);
    inventoryDateWeek = findViewById(R.id.tv_inventory_date_week);
    inventoryContainer = findViewById(R.id.inventory_date_container);
    horizontalLine = findViewById(R.id.inventory_date_line);
    checkmarkIcon = findViewById(R.id.inventory_checkmark);
  }

  public void populate(SelectInventoryViewModel selectInventoryViewModel) {
    DateTime date = new DateTime(selectInventoryViewModel.getInventoryDate());

    inventoryDateTime
        .setVisibility(selectInventoryViewModel.isShowTime() ? View.VISIBLE : View.INVISIBLE);
    inventoryDateTime.setText(date.toString(DateUtil.TIME_FORMAT));
    inventoryDateDay.setText(date.dayOfMonth().getAsText());
    inventoryDateMonth.setText(date.monthOfYear().getAsShortText());
    inventoryDateWeek.setText(date.dayOfWeek().getAsText());
  }


  @Override
  public void setChecked(boolean checked) {
    if (mChecked == checked) {
      return;
    }

    if (checked) {
      setSelected();
    } else {
      setDeSelected();
    }

    this.mChecked = checked;
  }

  private void setDeSelected() {
    inventoryContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    inventoryDateWeek.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    inventoryDateTime.setTextColor(getResources().getColor(R.color.color_text_primary));
    inventoryDateDay.setTextColor(getResources().getColor(R.color.color_text_primary));
    inventoryDateMonth.setTextColor(getResources().getColor(R.color.color_text_primary));
    inventoryDateWeek.setTextColor(getResources().getColor(R.color.color_text_primary));
    horizontalLine.setVisibility(View.VISIBLE);
    checkmarkIcon.setVisibility(View.GONE);
  }

  private void setSelected() {
    inventoryContainer.setBackgroundColor(getResources().getColor(R.color.color_teal));
    inventoryDateWeek.setBackgroundColor(getResources().getColor(R.color.color_teal_dark));
    inventoryDateTime.setTextColor(getResources().getColor(R.color.color_white));
    inventoryDateDay.setTextColor(getResources().getColor(R.color.color_white));
    inventoryDateMonth.setTextColor(getResources().getColor(R.color.color_white));
    inventoryDateWeek.setTextColor(getResources().getColor(R.color.color_white));
    horizontalLine.setVisibility(View.GONE);
    checkmarkIcon.setVisibility(View.VISIBLE);
  }

  @Override
  public boolean isChecked() {
    return mChecked;
  }

  @Override
  public void toggle() {
    setChecked(!mChecked);
  }

}
