package org.openlmis.core.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.openlmis.core.R;

import lombok.Setter;

public class SimpleSelectDialogFragment extends BaseDialogFragment {

    public static final String SELECTIONS = "selections";

    @Setter
    private AdapterView.OnItemClickListener movementTypeOnClickListener;
    private String[] selections;

    public SimpleSelectDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selections = getArguments().getStringArray(SELECTIONS);
        }
    }

    public SimpleSelectDialogFragment(AdapterView.OnItemClickListener movementTypeOnClickListener, String[] selections) {
        this.movementTypeOnClickListener = movementTypeOnClickListener;
        this.selections = selections;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.item_movement_type, R.id.tv_option, selections);

        builder.setAdapter(adapter, null);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getListView().setOnItemClickListener(movementTypeOnClickListener);
        return alertDialog;
    }
}
