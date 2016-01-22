package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ViaKitView extends LinearLayout {

    @InjectView(R.id.et_via_kit_received_hf)
    TextView etKitReceivedHF;

    @InjectView(R.id.et_via_kit_received_chw)
    TextView etKitReceivedCHW;

    @InjectView(R.id.et_via_kit_opened_hf)
    TextView etKitOpenedHF;

    @InjectView(R.id.et_via_kit_opened_chw)
    TextView etKitOpenedCHW;

    public ViaKitView(Context context) {
        super(context);
        init(context);
    }

    public ViaKitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_via_kit, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);

        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_kit)) {
            setVisibility(INVISIBLE);
        }
    }

    public void setValue(ViaKitsViewModel viaKitsViewModel) {
        etKitReceivedHF.setText(viaKitsViewModel.getKitsReceivedHF());
        etKitOpenedHF.setText(viaKitsViewModel.getKitsOpenedHF());
        etKitReceivedCHW.setText(viaKitsViewModel.getKitsReceivedCHW());
        etKitOpenedCHW.setText(viaKitsViewModel.getKitsOpenedCHW());
    }

}
