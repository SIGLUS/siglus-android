package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;

import lombok.Getter;
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

    @Getter
    boolean hasDataChanged;

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

    public boolean validate() {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_kit)) {
            return true;
        }

        return ViewUtil.checkEditTextEmpty(etKitReceivedHF)
                && ViewUtil.checkEditTextEmpty(etKitReceivedCHW)
                && ViewUtil.checkEditTextEmpty(etKitOpenedHF)
                && ViewUtil.checkEditTextEmpty(etKitOpenedCHW);
    }

    public void setValue(ViaKitsViewModel viaKitsViewModel) {
        etKitReceivedHF.setText(viaKitsViewModel.getKitsReceivedHF());
        etKitOpenedHF.setText(viaKitsViewModel.getKitsOpenedHF());
        etKitReceivedCHW.setText(viaKitsViewModel.getKitsReceivedCHW());
        etKitOpenedCHW.setText(viaKitsViewModel.getKitsOpenedCHW());
    }

    public void addTextChangeListeners(ViaKitsViewModel viaKitsViewModel) {
        etKitOpenedCHW.addTextChangedListener(new KitsOpenedCHWTextWatcher(viaKitsViewModel));
        etKitOpenedHF.addTextChangedListener(new KitsOpenedHFTextWatcher(viaKitsViewModel));
        etKitReceivedCHW.addTextChangedListener(new KitsReceivedCHWTextWatcher(viaKitsViewModel));
        etKitReceivedHF.addTextChangedListener(new KitsReceivedHFTextWatcher(viaKitsViewModel));
    }

    class KitsChangedTextWatcher extends SingleTextWatcher {
        protected ViaKitsViewModel viaKitsViewModel;

        public KitsChangedTextWatcher(ViaKitsViewModel viaKitsViewModel) {
            this.viaKitsViewModel = viaKitsViewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
        }
    }

    class KitsOpenedCHWTextWatcher extends KitsChangedTextWatcher {

        public KitsOpenedCHWTextWatcher(ViaKitsViewModel viaKitsViewModel) {
            super(viaKitsViewModel);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
            String value = editable.toString();
            viaKitsViewModel.setKitsOpenedCHW(value);
        }
    }

    class KitsOpenedHFTextWatcher extends KitsChangedTextWatcher {

        public KitsOpenedHFTextWatcher(ViaKitsViewModel viaKitsViewModel) {
            super(viaKitsViewModel);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
            String value = editable.toString();
            viaKitsViewModel.setKitsOpenedHF(value);
        }
    }

    class KitsReceivedHFTextWatcher extends KitsChangedTextWatcher {

        public KitsReceivedHFTextWatcher(ViaKitsViewModel viaKitsViewModel) {
            super(viaKitsViewModel);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
            String value = editable.toString();
            viaKitsViewModel.setKitsReceivedHF(value);
        }
    }

    class KitsReceivedCHWTextWatcher extends KitsChangedTextWatcher {

        public KitsReceivedCHWTextWatcher(ViaKitsViewModel viaKitsViewModel) {
            super(viaKitsViewModel);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
            String value = editable.toString();
            viaKitsViewModel.setKitsReceivedCHW(value);
        }
    }
}
