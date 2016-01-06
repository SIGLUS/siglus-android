package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.ViewUtil;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ViaKitView extends LinearLayout {

    @InjectView(R.id.et_via_kit_received_hf)
    EditText etKitReceivedHF;

    @InjectView(R.id.et_via_kit_received_chw)
    EditText etKitReceivedCHW;

    @InjectView(R.id.et_via_kit_opened_hf)
    EditText etKitOpenedHF;

    @InjectView(R.id.et_via_kit_opened_chw)
    EditText etKitOpenedCHW;

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
        LayoutInflater.from(context).inflate(R.layout.via_kit_view, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);

        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_show_kit_on_via_rnr_372)) {
            setVisibility(INVISIBLE);
        }
    }

    public boolean validate() {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_show_kit_on_via_rnr_372)) {
            return true;
        }

        return ViewUtil.checkEditTextEmpty(etKitReceivedHF)
                && ViewUtil.checkEditTextEmpty(etKitReceivedCHW)
                && ViewUtil.checkEditTextEmpty(etKitOpenedHF)
                && ViewUtil.checkEditTextEmpty(etKitOpenedCHW);
    }
}
