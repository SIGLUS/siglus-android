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


public class BaseDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_BUTTON = "positive";
    private static final String ARG_NEGATIVE_BUTTON = "negative";

    public String title;
    public String message;
    public String positiveText;
    public String negativeText;

    protected static Bundle bundle;
    private String tag;

    public static BaseDialogFragment newInstance() {
        BaseDialogFragment frag = new BaseDialogFragment();
        bundle = new Bundle();
        frag.setArguments(bundle);
        return frag;
    }

    private MsgDialogCallBack mListener;

    public void setCallBackListener(MsgDialogCallBack listener) {
        mListener = listener;
    }

    public BaseDialogFragment setTitle(String title){
        bundle.putString(ARG_TITLE, title);
        return this;
    }

    public BaseDialogFragment setMessage(String message){
        bundle.putString(ARG_MESSAGE, message);
        return this;
    }

    public BaseDialogFragment setPositiveText(String positiveText){
        bundle.putString(ARG_POSITIVE_BUTTON, positiveText);
        return this;
    }

    public BaseDialogFragment setNegativeText(String negativeText){
        bundle.putString(ARG_NEGATIVE_BUTTON, negativeText);
        return this;
    }

    public BaseDialogFragment setTag(String tag){
        bundle.putString("tag",tag);
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        title = arguments.getString(ARG_TITLE);
        message = arguments.getString(ARG_MESSAGE);
        positiveText = arguments.getString(ARG_POSITIVE_BUTTON);
        negativeText = arguments.getString(ARG_NEGATIVE_BUTTON);
        tag = arguments.getString("tag");
    }


    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof MsgDialogCallBack) {
            mListener = (MsgDialogCallBack) activity;
        }
        super.onAttach(activity);
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
        if (negativeText != null && !negativeText.isEmpty()) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (mListener != null) {
                            mListener.negativeClick(tag);
                        }
                    } catch (Exception exception) {
                        throw new ClassCastException("Must implement BaseDialogFragment.NegativeListener");
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
