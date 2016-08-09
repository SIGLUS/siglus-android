package org.openlmis.core.view.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.openlmis.core.R;

public class SimpleSelectDialogFragment extends BaseDialogFragment{

    private final AdapterView.OnItemClickListener movementTypeOnClickListener;
    private final String[] selections;
    private Context context;

    public SimpleSelectDialogFragment(Context context, AdapterView.OnItemClickListener movementTypeOnClickListener, String[] selections) {
        this.context = context;
        this.movementTypeOnClickListener = movementTypeOnClickListener;
        this.selections = selections;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.item_movement_type, R.id.text, selections);

        builder.setAdapter(adapter, null);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getListView().setOnItemClickListener(movementTypeOnClickListener);
        return alertDialog;
    }


    public interface SelectorOnClickListener extends DialogInterface.OnClickListener {
        void onClick(DialogInterface dialogInterface, int selectedItem);
    }
}
