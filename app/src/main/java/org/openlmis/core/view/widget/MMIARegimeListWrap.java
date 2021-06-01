package org.openlmis.core.view.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.presenter.MMIARequisitionPresenter;

import java.util.List;

public class MMIARegimeListWrap extends LinearLayout {
    private LayoutInflater layoutInflater;
    private MMIARegimeList regimeList;
    private LinearLayout regimeLeftHeader;
    private TextView leftHeaderAdult;
    private TextView leftHeaderChildren;

    public MMIARegimeListWrap(Context context) {
        super(context);
        init(context);
    }

    public MMIARegimeListWrap(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        layoutInflater = LayoutInflater.from(context);
        regimeList = (MMIARegimeList) layoutInflater.inflate(R.layout.fragment_mmia_requisition_regime_list_conent, this, false);
        regimeLeftHeader = (LinearLayout) layoutInflater.inflate(R.layout.fragment_mmia_requisition_regime_left_header, this, false);
        leftHeaderAdult = (TextView) regimeLeftHeader.findViewById(R.id.regime_left_header_adult);
        leftHeaderChildren = (TextView) regimeLeftHeader.findViewById(R.id.regime_left_header_children);
    }


    public void initView(TextView totalView, TextView totalPharmacy, TextView tvTotalPharmacyTitle, MMIARequisitionPresenter presenter) {
        regimeList.initView(totalView, totalPharmacy, tvTotalPharmacyTitle, presenter);
        addView(regimeLeftHeader);
        addView(regimeList);
        leftHeaderAdult.setBackgroundResource(R.color.color_green_light);
        leftHeaderChildren.setBackgroundResource(R.color.color_regime_baby);

        if (regimeList.isPharmacyEmpty) {
            regimeLeftHeader.setVisibility(GONE);
        }
        regimeList.post(this::updateLeftHeader);
    }

    public void updateLeftHeader() {
        LayoutParams adultParams = (LayoutParams) leftHeaderAdult.getLayoutParams();
        adultParams.height = regimeList.adultHeight;
        leftHeaderAdult.setLayoutParams(adultParams);
        LayoutParams childrenParams = (LayoutParams) leftHeaderChildren.getLayoutParams();
        childrenParams.height = regimeList.childrenHeight;
        leftHeaderChildren.setLayoutParams(childrenParams);
    }

    public List<RegimenItem> getDataList() {
        return regimeList.getDataList();
    }

    public boolean isCompleted() {
        return regimeList.isCompleted();
    }

    public void deHighLightTotal() {
        regimeList.deHighLightTotal();
    }

    public void addCustomRegimenItem(Regimen regimen) {
        regimeList.addCustomRegimenItem(regimen);
    }

    public void setRegimeListener(MMIARegimeList.MMIARegimeListener regimeListener) {
        regimeList.setRegimeListener(regimeListener);
    }
}
