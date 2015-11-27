package org.openlmis.core.view.fragment;

import android.os.Bundle;

import roboguice.fragment.provided.RoboDialogFragment;

public class BaseDialogFragment extends RoboDialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
