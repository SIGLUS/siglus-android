package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.openlmis.core.R;

import lombok.Getter;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

@Getter
public class ActionPanelView extends FrameLayout {
    @InjectView(R.id.btn_save)
    View btnSave;

    @InjectView(R.id.btn_complete)
    Button btnComplete;

    public ActionPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        inflate(context, R.layout.view_action_panel, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void setListener(OnClickListener positiveClickListener, OnClickListener negativeClickListener) {
        btnComplete.setOnClickListener(positiveClickListener);
        btnSave.setOnClickListener(negativeClickListener);
    }

    public void setPositiveButtonText(String buttonName) {
        btnComplete.setText(buttonName);
    }

    public void setNegativeButtonVisibility(int visibility) {
        btnSave.setVisibility(visibility);
    }
}
