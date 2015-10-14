package org.openlmis.core.view.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import org.openlmis.core.R;

public class MMIATotalMismatchDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.msg_regime_total_and_patient_total_not_match)
                .setPositiveButton("OK", null)
                .create();
    }
}
