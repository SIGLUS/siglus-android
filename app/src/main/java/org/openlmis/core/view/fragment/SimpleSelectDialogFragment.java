package org.openlmis.core.view.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class SimpleSelectDialogFragment extends BaseDialogFragment{

    private final SelectorOnClickListener movementTypeOnClickListener;
    private final String[] selections;

    public SimpleSelectDialogFragment(SelectorOnClickListener movementTypeOnClickListener, String[] selections) {
        this.movementTypeOnClickListener = movementTypeOnClickListener;
        this.selections = selections;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(selections, -1, movementTypeOnClickListener);

        final AlertDialog alertDialog = builder.create();
        return alertDialog;
    }


    public interface SelectorOnClickListener extends DialogInterface.OnClickListener {
        void onClick(DialogInterface dialogInterface, int selectedItem);
    }
}
