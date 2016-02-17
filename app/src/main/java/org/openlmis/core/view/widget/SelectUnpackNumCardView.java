package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import org.openlmis.core.R;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SelectUnpackNumCardView extends CardView implements Checkable {

    private boolean mChecked;

    @InjectView(R.id.tv_unpack_num_container)
    private View unpackNumContainer;

    @InjectView(R.id.unpack_num_line)
    private View horizontalLine;

    @InjectView(R.id.iv_checkmark)
    private ImageView checkmarkIcon;

    @InjectView(R.id.tv_unpack_type)
    public TextView tvUnpackType;

    @InjectView(R.id.tv_unpack_num)
    public TextView tvUnpackNum;


    public SelectUnpackNumCardView(Context context) {
        super(context);
        init();
    }

    public SelectUnpackNumCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_unpack_num_card, this);
        setRadius(getResources().getDimension(R.dimen.cardview_radius));

        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = getWidth();
                setLayoutParams(layoutParams);
            }
        });

        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void populate(int num, String kitType) {
        tvUnpackNum.setText(String.valueOf(num));
        tvUnpackType.setText(kitType);
    }


    @Override
    public void setChecked(boolean checked) {
        if (mChecked == checked) {
            return;
        }

        if (checked) {
            setSelected();
        } else {
            setDeSelected();
        }

        this.mChecked = checked;
    }

    private void setDeSelected() {
        unpackNumContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvUnpackType.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        tvUnpackNum.setTextColor(getResources().getColor(R.color.color_text_primary));
        tvUnpackType.setTextColor(getResources().getColor(R.color.color_text_primary));
        horizontalLine.setVisibility(View.VISIBLE);
        checkmarkIcon.setVisibility(View.GONE);
    }

    private void setSelected() {
        unpackNumContainer.setBackgroundColor(getResources().getColor(R.color.color_teal));
        tvUnpackType.setBackgroundColor(getResources().getColor(R.color.color_teal_dark));
        tvUnpackNum.setTextColor(getResources().getColor(R.color.color_white));
        tvUnpackType.setTextColor(getResources().getColor(R.color.color_white));
        horizontalLine.setVisibility(View.GONE);
        checkmarkIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

}
