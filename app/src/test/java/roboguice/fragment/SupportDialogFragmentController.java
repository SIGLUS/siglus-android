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

package roboguice.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.ComponentController;
import org.robolectric.util.ReflectionHelpers;
import roboguice.actiivty.FragmentControllerActivity;

public class SupportDialogFragmentController<F extends DialogFragment> extends
    ComponentController<SupportDialogFragmentController<F>, F> {

  public static final String FRAGMENT_SHOW_TAG = SupportDialogFragmentController.class.getSimpleName();

  private final F dialogFragment;
  private final ActivityController<? extends FragmentActivity> activityController;

  protected SupportDialogFragmentController(F dialogFragment, Class<? extends FragmentActivity> activityClass) {
    this(dialogFragment, activityClass, null);
  }

  protected SupportDialogFragmentController(
      F dialogFragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    super(dialogFragment, intent);
    this.dialogFragment = dialogFragment;
    this.activityController = Robolectric.buildActivity(activityClass, intent);
  }

  public static <F extends DialogFragment> SupportDialogFragmentController<F> of(F fragment) {
    return new SupportDialogFragmentController<>(fragment, FragmentControllerActivity.class);
  }

  public static <T extends DialogFragment> SupportDialogFragmentController<T> of(Class<T> fragmentClass) {
    return SupportDialogFragmentController.of(ReflectionHelpers.callConstructor(fragmentClass));
  }

  public static <F extends DialogFragment> SupportDialogFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass) {
    return new SupportDialogFragmentController<>(fragment, activityClass);
  }

  public static <F extends DialogFragment> SupportDialogFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    return new SupportDialogFragmentController<>(fragment, activityClass, intent);
  }

  public static <T extends DialogFragment> SupportDialogFragmentController<T> of(Class<T> fragmentClass,
      Bundle params) {
    final T fragment = ReflectionHelpers.callConstructor(fragmentClass);
    fragment.setArguments(params);
    return SupportDialogFragmentController.of(fragment);
  }

  public F setupDialogFragment() {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            dialogFragment.show(activityController.create().start().resume().get().getSupportFragmentManager(), FRAGMENT_SHOW_TAG);
          }
        });
    return dialogFragment;
  }

  public F setupDialogFragment(final Bundle bundle) {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            dialogFragment.show(activityController.create(bundle).start().resume().get().getSupportFragmentManager(), FRAGMENT_SHOW_TAG);
          }
        });
    return dialogFragment;
  }

  @Override
  public SupportDialogFragmentController<F> create() {
    return this;
  }

  @Override
  public SupportDialogFragmentController<F> destroy() {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            dialogFragment.dismiss();
            activityController.pause().stop().destroy();
          }
        });
    return this;
  }
}
