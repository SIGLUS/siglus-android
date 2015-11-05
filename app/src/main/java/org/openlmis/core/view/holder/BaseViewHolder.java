package org.openlmis.core.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;

import roboguice.inject.InjectView;

public class BaseViewHolder extends RecyclerView.ViewHolder {
    private ArrayList<ViewMembersInjector> viewsForInjection;

    public BaseViewHolder(View itemView) {
        super(itemView);
        prepareFields();
        injectViews(itemView);
    }

    private void prepareFields() {
        viewsForInjection = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectView.class)) {
                viewsForInjection.add(new ViewMembersInjector(field, field.getAnnotation(InjectView.class)));
            }
        }
    }

    private void injectViews(View v) {
        for (ViewMembersInjector viewMembersInjector : viewsForInjection) {
            viewMembersInjector.reallyInjectMembers(this, v);
        }
    }

    class ViewMembersInjector {

        private Field field;

        private InjectView annotation;

        public ViewMembersInjector(Field field, InjectView annotation) {
            this.field = field;
            this.annotation = annotation;
        }

        public void reallyInjectMembers(BaseViewHolder holder, View view) {
            Object value = null;
            try {
                value = view.findViewById(annotation.value());
                if (value == null) {
                    throw new NullPointerException(String.format("Can't inject null value into %s.%s when field is not @Nullable", field.getDeclaringClass(), field.getName()));
                }
                field.setAccessible(true);
                field.set(holder, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException f) {
                throw new IllegalArgumentException(String.format("Can't assign %s value %s to %s field %s", value != null ? value.getClass() : "(null)", value, field.getType(), field.getName()));
            }
        }
    }

}
