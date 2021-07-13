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

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.apache.commons.lang3.reflect.FieldUtils;
import roboguice.inject.InjectView;

@SuppressWarnings("PMD")
public class BaseViewHolder extends RecyclerView.ViewHolder {

  private ArrayList<ViewMembersInjector> viewsForInjection;

  protected Context context;

  public BaseViewHolder(View itemView) {
    super(itemView);
    this.context = itemView.getContext();
    prepareFields();
    injectViews(itemView);
  }

  private void prepareFields() {
    viewsForInjection = new ArrayList<>();
    for (Field field : FieldUtils.getAllFields(this.getClass())) {
      if (field.isAnnotationPresent(InjectView.class)) {
        viewsForInjection
            .add(new ViewMembersInjector(field, field.getAnnotation(InjectView.class)));
      }
    }
  }

  private void injectViews(View v) {
    for (ViewMembersInjector viewMembersInjector : viewsForInjection) {
      viewMembersInjector.reallyInjectMembers(this, v);
    }
  }

  static class ViewMembersInjector {

    private final Field field;

    private final InjectView annotation;

    public ViewMembersInjector(Field field, InjectView annotation) {
      this.field = field;
      this.annotation = annotation;
    }

    public void reallyInjectMembers(BaseViewHolder holder, View view) {
      Object value = null;
      try {
        value = view.findViewById(annotation.value());
        if (value != null) {
          field.setAccessible(true);
          field.set(holder, value);
        }
      } catch (Exception f) {
        throw new IllegalArgumentException(String.format("Can't assign %s value %s to %s field %s",
            value != null ? value.getClass() : "(null)", value, field.getType(), field.getName()));
      }
    }
  }

}
