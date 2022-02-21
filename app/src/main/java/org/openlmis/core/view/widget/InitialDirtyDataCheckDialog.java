package org.openlmis.core.view.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

public class InitialDirtyDataCheckDialog extends BaseDialogFragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_initial_dirty_data_check, container, false);
  }

  public void show(FragmentManager manager) {
    if (manager.findFragmentByTag("initial_dirty_data_check_dialog") != null) {
      return;
    }
    super.show(manager, "initial_dirty_data_check_dialog");
  }
}
