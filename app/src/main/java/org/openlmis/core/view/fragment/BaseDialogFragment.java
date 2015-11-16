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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;


public class BaseDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_BUTTON = "positive";
    private static final String ARG_NEGATIVE_BUTTON = "negative";
    private static final String ARG_TAG = "tag";

    private String title;
    private String message;
    private String positiveText;
    private String negativeText;
    private String tag;

    private MsgDialogCallBack mListener;

    public static BaseDialogFragment newInstance(String title, String message, String positiveText, String tag) {
        return newInstance(title, message, positiveText, null, tag);
    }

    public static BaseDialogFragment newInstance(String title, String message, String positiveText, String negativeText, String tag) {
        Bundle bundle = new Bundle();

        bundle.putString(ARG_TITLE, title);
        bundle.putString(ARG_MESSAGE, message);
        bundle.putString(ARG_POSITIVE_BUTTON, positiveText);
        bundle.putString(ARG_NEGATIVE_BUTTON, negativeText);
        bundle.putString(ARG_TAG, tag);

        BaseDialogFragment fragment = new BaseDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setCallBackListener(MsgDialogCallBack listener) {
        mListener = listener;
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
        title = arguments.getString(ARG_TITLE);
        message = arguments.getString(ARG_MESSAGE);
        positiveText = arguments.getString(ARG_POSITIVE_BUTTON);
        negativeText = arguments.getString(ARG_NEGATIVE_BUTTON);
        tag = arguments.getString(ARG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.positiveClick(tag);
                        }
                    }
                });
        if (!TextUtils.isEmpty(negativeText)) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) {
                        mListener.negativeClick(tag);
                    }
                }
            });
        }

        return builder.create();
    }

    public interface MsgDialogCallBack {
        void positiveClick(String tag);

        void negativeClick(String tag);
    }

}
