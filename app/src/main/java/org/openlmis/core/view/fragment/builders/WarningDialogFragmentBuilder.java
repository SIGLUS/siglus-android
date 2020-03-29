package org.openlmis.core.view.fragment.builders;

import com.google.inject.Inject;

import org.openlmis.core.view.fragment.WarningDialogFragment;

public class WarningDialogFragmentBuilder {

    @Inject
    public WarningDialogFragmentBuilder() {
    }

    public WarningDialogFragment build(WarningDialogFragment.DialogDelegate delegate,
                                       int message, int positiveMessageButton,
                                       int negativeMessageButton) {

        WarningDialogFragment wipeDataDialog = WarningDialogFragment.newInstance(
                message, positiveMessageButton, negativeMessageButton);
        wipeDataDialog.setDelegate(delegate);
        return wipeDataDialog;
    }

    public WarningDialogFragment build(WarningDialogFragment.DialogDelegate delegate,
                                       String message, String positiveMessageButton,
                                       String negativeMessageButton) {

        WarningDialogFragment wipeDataDialog = WarningDialogFragment.newInstance(
                message, positiveMessageButton, negativeMessageButton);
        wipeDataDialog.setDelegate(delegate);
        return wipeDataDialog;
    }

}
