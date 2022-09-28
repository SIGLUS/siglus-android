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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import roboguice.fragment.RoboMigrationAndroidXDialogFragment;

public class SimpleDialogFragment extends RoboMigrationAndroidXDialogFragment {

  private static final String ARG_TITLE = "title";
  private static final String ARG_MESSAGE = "message";
  private static final String ARG_POSITIVE_BUTTON = "positive";
  private static final String ARG_NEGATIVE_BUTTON = "negative";
  private static final String ARG_TAG = "tag";

  private CharSequence title;
  private CharSequence message;
  private String positiveText;
  private String negativeText;
  private String tag;

  @Getter
  private MsgDialogCallBack mListener;

  public static SimpleDialogFragment newInstance(String title, CharSequence message, String positiveText) {
    return newInstance(title, message, positiveText, null, null);
  }

  public static SimpleDialogFragment newInstance(String title, CharSequence message, String positiveText, String tag) {
    return newInstance(title, message, positiveText, null, tag);
  }

  public static SimpleDialogFragment newInstance(CharSequence message) {
    return newInstance(null, message, LMISApp.getContext().getString(R.string.btn_positive),
        LMISApp.getContext().getString(R.string.btn_negative), null);
  }

  public static SimpleDialogFragment newInstance(CharSequence title, CharSequence message,
      String positiveText, String negativeText, String tag) {
    Bundle bundle = new Bundle();

    bundle.putCharSequence(ARG_TITLE, title);
    bundle.putCharSequence(ARG_MESSAGE, message);
    bundle.putString(ARG_POSITIVE_BUTTON, positiveText);
    bundle.putString(ARG_NEGATIVE_BUTTON, negativeText);
    bundle.putString(ARG_TAG, tag);

    SimpleDialogFragment fragment = new SimpleDialogFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  public void setCallBackListener(MsgDialogCallBack listener) {
    mListener = listener;
  }

  @Override
  public void show(@NonNull FragmentManager manager, String tag) {
    //avoid the duplicate Dialog
    if (manager.findFragmentByTag(tag) != null) {
      return;
    }
    super.show(manager, tag);
  }

  @Override
  public void onAttach(Activity activity) {
    if ((activity instanceof MsgDialogCallBack) && mListener == null) {
      mListener = (MsgDialogCallBack) activity;
    }
    super.onAttach(activity);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Bundle arguments = getArguments();
    title = arguments.getCharSequence(ARG_TITLE);
    message = arguments.getCharSequence(ARG_MESSAGE);
    positiveText = arguments.getString(ARG_POSITIVE_BUTTON);
    negativeText = arguments.getString(ARG_NEGATIVE_BUTTON);
    tag = arguments.getString(ARG_TAG);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialog)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText, (dialog, which) -> {
          if (mListener != null) {
            mListener.positiveClick(tag);
          }
        });
    if (hasNegativeButton()) {
      builder.setNegativeButton(negativeText, (dialog, which) -> {
        if (mListener != null) {
          mListener.negativeClick(tag);
        }
      });
    }

    final AlertDialog alertDialog = builder.create();
    changeUserInterface(alertDialog);
    return alertDialog;
  }

  private void changeUserInterface(AlertDialog alertDialog) {
    alertDialog.setOnShowListener(dialog -> {
      Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
      positiveButton.setTypeface(null, Typeface.BOLD);
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT,
          (int) getResources().getDimension(R.dimen.button_height_default));
      positiveButton.setLayoutParams(layoutParams);
      positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_accent));

      if (hasNegativeButton()) {
        final Button negativeButton = ((AlertDialog) dialog)
            .getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTypeface(null, Typeface.BOLD);
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_accent));
      }

      TextView textView = ((AlertDialog) dialog).findViewById(android.R.id.message);
      textView.setTextSize(20);
    });
  }

  private boolean hasNegativeButton() {
    return !TextUtils.isEmpty(negativeText);
  }

  public interface MsgDialogCallBack {

    void positiveClick(String tag);

    void negativeClick(String tag);
  }

}
