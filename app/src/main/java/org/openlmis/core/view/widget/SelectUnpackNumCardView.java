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
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import org.openlmis.core.R;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SelectUnpackNumCardView extends CardView implements Checkable {

  private boolean mChecked;

  @InjectView(R.id.tv_unpack_num_container)
  private View unpackNumContainer;

  @InjectView(R.id.unpack_num_line)
  private View horizontalLine;

  @InjectView(R.id.iv_checkmark)
  private ImageView checkmarkIcon;

  @InjectView(R.id.tv_unpack_type)
  public TextView tvUnpackType;

  @InjectView(R.id.tv_unpack_num)
  public TextView tvUnpackNum;


  public SelectUnpackNumCardView(Context context) {
    super(context);
    init();
  }

  public SelectUnpackNumCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    inflate(getContext(), R.layout.view_unpack_num_card, this);
    setRadius(getResources().getDimension(R.dimen.cardview_radius));

    post(() -> {
      ViewGroup.LayoutParams layoutParams = getLayoutParams();
      layoutParams.height = getWidth();
      setLayoutParams(layoutParams);
    });

    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  public void populate(int num, String kitType) {
    tvUnpackNum.setText(String.valueOf(num));
    tvUnpackType.setText(kitType);
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
    unpackNumContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    tvUnpackType.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    tvUnpackNum.setTextColor(getResources().getColor(R.color.color_text_primary));
    tvUnpackType.setTextColor(getResources().getColor(R.color.color_text_primary));
    horizontalLine.setVisibility(View.VISIBLE);
    checkmarkIcon.setVisibility(View.GONE);
  }

  private void setSelected() {
    unpackNumContainer.setBackgroundColor(getResources().getColor(R.color.color_teal));
    tvUnpackType.setBackgroundColor(getResources().getColor(R.color.color_teal_dark));
    tvUnpackNum.setTextColor(getResources().getColor(R.color.color_white));
    tvUnpackType.setTextColor(getResources().getColor(R.color.color_white));
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
