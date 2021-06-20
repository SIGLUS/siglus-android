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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.ComponentController;
import org.robolectric.util.ReflectionHelpers;
import roboguice.actiivty.FragmentControllerActivity;

public class SupportFragmentController<F extends Fragment>
    extends ComponentController<SupportFragmentController<F>, F> {

  private final F fragment;
  private final ActivityController<? extends FragmentActivity> activityController;

  protected SupportFragmentController(F fragment, Class<? extends FragmentActivity> activityClass) {
    this(fragment, activityClass, null);
  }

  protected SupportFragmentController(
      F fragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    super(fragment, intent);
    this.fragment = fragment;
    this.activityController = Robolectric.buildActivity(activityClass, intent);
  }

  public static <F extends Fragment> SupportFragmentController<F> of(F fragment) {
    return new SupportFragmentController<>(fragment, FragmentControllerActivity.class);
  }

  public static <T extends Fragment> SupportFragmentController<T> of(Class<T> fragmentClass) {
    return SupportFragmentController.of(ReflectionHelpers.callConstructor(fragmentClass));
  }

  public static <F extends Fragment> SupportFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass) {
    return new SupportFragmentController<>(fragment, activityClass);
  }

  public static <F extends Fragment> SupportFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    return new SupportFragmentController<>(fragment, activityClass, intent);
  }

  public static <T extends Fragment> SupportFragmentController<T> of(Class<T> fragmentClass, Bundle params) {
    final T fragment = ReflectionHelpers.callConstructor(fragmentClass);
    fragment.setArguments(params);
    return SupportFragmentController.of(fragment);
  }

  /**
   * Sets up the given fragment by attaching it to an activity, calling its onCreate() through onResume() lifecycle
   * methods, and then making it visible. Note that the fragment will be added to the view with ID 1.
   */
  public static <F extends Fragment> F setupFragment(F fragment) {
    return SupportFragmentController.of(fragment).create().start().resume().visible().get();
  }

  public static <T extends Fragment> T setupFragment(Class<T> fragmentClass) {
    return SupportFragmentController.of(ReflectionHelpers.callConstructor(fragmentClass))
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity, calling its onCreate() through onResume() lifecycle
   * methods, and then making it visible. Note that the fragment will be added to the view with ID 1.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment, Class<? extends FragmentActivity> fragmentActivityClass) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity created with the given bundle, calling its onCreate()
   * through onResume() lifecycle methods, and then making it visible. Note that the fragment will be added to the view
   * with ID 1.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment, Class<? extends FragmentActivity> fragmentActivityClass, Bundle bundle) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create(bundle)
        .start()
        .resume()
        .visible()
        .get();
  }

  public static <T extends Fragment> T setupFragment(Class<T> fragmentClass, Bundle bundle) {
    return SupportFragmentController.of(ReflectionHelpers.callConstructor(fragmentClass))
        .create(bundle)
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity created with the given bundle and container id, calling
   * its onCreate() through onResume() lifecycle methods, and then making it visible.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment,
      Class<? extends FragmentActivity> fragmentActivityClass,
      int containerViewId,
      Bundle bundle) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create(containerViewId, bundle)
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to the view with ID {@code contentViewId}.
   */
  public SupportFragmentController<F> create(final int contentViewId, final Bundle bundle) {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            activityController
                .create(bundle)
                .get()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(contentViewId, fragment)
                .commitNow();
          }
        });
    return this;
  }

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to it. Note that the fragment will be added to the
   * view with ID 1.
   */
  public SupportFragmentController<F> create(final Bundle bundle) {
    return create(FragmentControllerActivity.ID_CONTAINER, bundle);
  }

  @Override
  public SupportFragmentController<F> create() {
    return create(FragmentControllerActivity.ID_CONTAINER, null);
  }

  @Override
  public SupportFragmentController<F> destroy() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.destroy();
      }
    });
    return this;
  }

  public SupportFragmentController<F> start() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.start();
      }
    });
    return this;
  }

  public SupportFragmentController<F> resume() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.resume();
      }
    });
    return this;
  }

  public SupportFragmentController<F> pause() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.pause();
      }
    });
    return this;
  }

  public SupportFragmentController<F> stop() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.stop();
      }
    });
    return this;
  }

  public SupportFragmentController<F> visible() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.visible();
      }
    });
    return this;
  }
}
