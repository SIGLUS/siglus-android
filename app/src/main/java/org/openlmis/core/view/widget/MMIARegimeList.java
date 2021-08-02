/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.SelectRegimeProductsActivity;
import org.openlmis.core.view.fragment.MMIARequisitionFragment;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import rx.Subscriber;

public class MMIARegimeList extends LinearLayout {

  private Context context;
  private TextView totalView;
  private TextView totalPharmacy;
  private TextView totalPharmacyTitle;
  private List<RegimenItem> dataList;
  private List<EditText> editTotalTexts;
  private List<EditText> editPharmacyTexts;
  private LayoutInflater layoutInflater;
  private ArrayList<RegimenItem> adults;
  private ArrayList<RegimenItem> paediatrics;
  protected MMIARequisitionPresenter presenter;

  public int adultHeight = 0;
  public int childrenHeight = 0;
  private MMIARegimeListener regimeListener;

  public boolean isPharmacyEmpty = false;

  public enum COUNTTYPE {
    AMOUNT,
    PHARMACY,
  }

  public MMIARegimeList(Context context) {
    super(context);
    init(context);
  }

  public MMIARegimeList(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    setOrientation(LinearLayout.VERTICAL);
    layoutInflater = LayoutInflater.from(context);
  }

  public void initView(TextView totalView, TextView totalPharmacy, TextView tvTotalPharmacyTitle,
      MMIARequisitionPresenter presenter) {
    this.presenter = presenter;
    this.isPharmacyEmpty = presenter.getRnRForm().getRegimenThreeLineListWrapper().isEmpty();
    this.dataList = presenter.getRnRForm().getRegimenItemListWrapper();
    this.editTotalTexts = new ArrayList<>();
    this.editPharmacyTexts = new ArrayList<>();
    initCategoryList(dataList);
    this.totalView = totalView;
    this.totalPharmacy = totalPharmacy;
    this.totalPharmacyTitle = tvTotalPharmacyTitle;

    for (int i = 0; i < adults.size(); i++) {
      addItemView(adults.get(i), i);
    }

    if (isCustomEnable()) {
      addAdultBtnView();
    }

    for (int i = 0; i < paediatrics.size(); i++) {
      addItemView(paediatrics.get(i), adults.size() + i);
    }

    if (isCustomEnable()) {
      addPaediatricsBtnView();
    }

    totalView.setText(String.valueOf(getTotal(COUNTTYPE.AMOUNT)));
    totalPharmacy.setText(String.valueOf(getTotal(COUNTTYPE.PHARMACY)));
    editTotalTexts.get(editTotalTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
    if (!editPharmacyTexts.isEmpty()) {
      editPharmacyTexts.get(editPharmacyTexts.size() - 1).setImeOptions(EditorInfo.IME_ACTION_DONE);
    }
    post(this::initAdultAndChildrenHeaderHeight);
  }

  public void initAdultAndChildrenHeaderHeight() {
    int childSize = getChildCount();

    int[] firstItemLocations = new int[2];
    int[] lastAdultLocations = new int[2];
    int[] lastItemLocations = new int[2];

    getChildAt(0).getLocationOnScreen(firstItemLocations);
    View lastAdult = getChildAt(isCustomEnable() ? adults.size(): adults.size() -1);
    lastAdult.getLocationOnScreen(lastAdultLocations);

    adultHeight = lastAdultLocations[1] - firstItemLocations[1] + lastAdult.getHeight();
    getChildAt(childSize - 1).getLocationOnScreen(lastItemLocations);
    childrenHeight =
        lastItemLocations[1] + getChildAt(childSize - 1).getHeight() - lastAdultLocations[1] - lastAdult.getHeight();
    ((MMIARegimeListWrap) getParent()).updateLeftHeader();
  }

  private boolean isCustomEnable() {
    return !presenter.getRnRForm().isAuthorized();
  }

  private void addAdultBtnView() {
    final TextView view = (TextView) layoutInflater
        .inflate(R.layout.item_add_custom_regime, this, false);
    view.setText(R.string.label_add_adult_regime);
    view.setBackgroundResource(R.color.color_green_light);
    view.setOnClickListener(v -> getFragment().startActivityForResult(
        SelectRegimeProductsActivity.getIntentToMe(view.getContext(), Regimen.RegimeType.Adults),
        MMIARequisitionFragment.REQUEST_FOR_CUSTOM_REGIME));
    addView(view);
  }

  private void addPaediatricsBtnView() {
    final TextView view = (TextView) layoutInflater
        .inflate(R.layout.item_add_custom_regime, this, false);
    view.setText(R.string.label_add_child_regime);
    view.setBackgroundResource(R.color.color_regime_baby);
    view.setOnClickListener(v -> getFragment().startActivityForResult(SelectRegimeProductsActivity
            .getIntentToMe(view.getContext(), Regimen.RegimeType.Paediatrics),
        MMIARequisitionFragment.REQUEST_FOR_CUSTOM_REGIME));
    addView(view);
  }

  private Fragment getFragment() {
    return ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.fragment_requisition);
  }

  private void initCategoryList(List<RegimenItem> regimenItems) {
    adults = new ArrayList<>();
    paediatrics = new ArrayList<>();

    for (RegimenItem item : regimenItems) {
      if (item == null || item.getRegimen() == null) {
        continue;
      }
      if (Regimen.RegimeType.Paediatrics.equals(item.getRegimen().getType())) {
        paediatrics.add(item);
      } else {
        adults.add(item);
      }
    }
  }

  public List<RegimenItem> getDataList() {
    return dataList;
  }

  private void addItemView(final RegimenItem item, final int position) {
    View view = layoutInflater.inflate(R.layout.item_regime, this, false);
    TextView regimeName = view.findViewById(R.id.regime_name);
    EditText regimeAmount = view.findViewById(R.id.regime_amount);
    EditText regimePharmacyAmount = view.findViewById(R.id.regime_pharmacy_amount);

    if (!isPharmacyEmpty) {
      editPharmacyTexts.add(regimePharmacyAmount);
      if (item.getPharmacy() != null) {
        regimePharmacyAmount.setText(String.valueOf(item.getPharmacy()));
      }
      regimePharmacyAmount.addTextChangedListener(new EditTextWatcher(item, COUNTTYPE.PHARMACY));
      regimePharmacyAmount
          .setOnEditorActionListener(getOnEditorActionListener(position, COUNTTYPE.PHARMACY));

    } else {
      regimePharmacyAmount.setVisibility(GONE);
    }

    editTotalTexts.add(regimeAmount);
    Regimen regimen = item.getRegimen();
    regimeName.setText(regimen.getName());

    if (item.getAmount() != null) {
      regimeAmount.setText(String.valueOf(item.getAmount()));
    }

    setBackground(view, regimen);

    regimeAmount.addTextChangedListener(new EditTextWatcher(item, COUNTTYPE.AMOUNT));

    regimeAmount.setOnEditorActionListener(getOnEditorActionListener(position, COUNTTYPE.AMOUNT));

    setDelIconForCustomRegime(item, view);
    addView(view);

  }

  private TextView.OnEditorActionListener getOnEditorActionListener(int position,
      COUNTTYPE counttype) {
    return (v, actionId, event) -> {
      if (COUNTTYPE.AMOUNT == counttype && actionId == EditorInfo.IME_ACTION_NEXT) {
        if ((position + 1) < editTotalTexts.size()) {
          editTotalTexts.get(position + 1).requestFocus();
          return true;
        }
      } else if (COUNTTYPE.PHARMACY == counttype && actionId == EditorInfo.IME_ACTION_NEXT) {
        if ((position + 1) < editPharmacyTexts.size()) {
          editPharmacyTexts.get(position + 1).requestFocus();
          return true;
        }
      } else {
        return false;
      }
      return false;
    };
  }

  private void setBackground(View view, Regimen regimen) {
    if (Regimen.RegimeType.Paediatrics.equals(regimen.getType())) {
      view.setBackgroundResource(R.color.color_regime_baby);
    } else {
      view.setBackgroundResource(R.color.color_green_light);
    }
  }

  private void setDelIconForCustomRegime(final RegimenItem item, View view) {
    if (item.getRegimen().isCustom() && isCustomEnable()) {
      View ivDel = ((ViewStub) view.findViewById(R.id.vs_del)).inflate();
      ivDel.setOnClickListener(v -> showDelConfirmDialog(item));
    }
  }

  protected void showDelConfirmDialog(final RegimenItem item) {
    SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        LMISApp.getContext().getString(R.string.msg_regime_del_confirm));
    dialogFragment.show(((BaseActivity) getContext()).getSupportFragmentManager(), "del_confirm_dialog");
    dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
      @Override
      public void positiveClick(String tag) {
        if (regimeListener != null) {
          regimeListener.loading();
        }
        presenter.deleteRegimeItem(item).subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            refreshRegimeView();
            if (regimeListener != null) {
              regimeListener.loaded();
            }
          }

          @Override
          public void onError(Throwable e) {
            ToastUtil.show(e.getMessage());
            if (regimeListener != null) {
              regimeListener.loaded();
            }
          }

          @Override
          public void onNext(Void aVoid) {
            // do nothing
          }
        });
      }

      @Override
      public void negativeClick(String tag) {
        // do nothing
      }
    });
  }

  public void deHighLightTotal() {
    totalView.setBackground(getResources().getDrawable(R.color.color_page_gray));
    if (isPharmacyEmpty) {
      totalPharmacy.setVisibility(GONE);
      totalPharmacyTitle.setVisibility(GONE);
    } else {
      totalPharmacy.setBackground(getResources().getDrawable(R.color.color_page_gray));
    }
  }

  public boolean hasEmptyField() {
    for (RegimenItem item : dataList) {
      if (null == item.getAmount()) {
        return true;
      }
    }
    return false;
  }

  public void refreshRegimeView() {
    removeAllViews();
    initView(totalView, totalPharmacy, totalPharmacyTitle, presenter);
  }

  public void addCustomRegimenItem(Regimen regimen) {
    if (presenter.isRegimeItemExists(regimen)) {
      ToastUtil.show(R.string.msg_regime_already_exist);
      return;
    }
    if (regimeListener != null) {
      regimeListener.loading();
    }
    presenter.addCustomRegimenItem(regimen).subscribe(customRegimenItemSubscriber());
  }

  private Subscriber<Void> customRegimenItemSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        refreshRegimeView();
        if (regimeListener != null) {
          regimeListener.loaded();
        }
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        if (regimeListener != null) {
          regimeListener.loaded();
        }
      }

      @Override
      public void onNext(Void data) {
        // do nothing
      }
    };
  }

  class EditTextWatcher implements android.text.TextWatcher {

    private final RegimenItem item;
    private final COUNTTYPE counttype;

    public EditTextWatcher(RegimenItem item, COUNTTYPE counttype) {
      this.item = item;
      this.counttype = counttype;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
      long count = 0L;
      try {
        count = Long.parseLong(editable.toString());
      } catch (NumberFormatException ignored) {
        // do nothing
      }
      if (COUNTTYPE.AMOUNT == counttype) {
        item.setAmount(count);
        totalView.setText(String.valueOf(getTotal(COUNTTYPE.AMOUNT)));
      } else if (COUNTTYPE.PHARMACY == counttype) {
        item.setPharmacy(count);
        totalPharmacy.setText(String.valueOf(getTotal(COUNTTYPE.PHARMACY)));
      }
    }
  }

  public boolean isCompleted() {
    for (int i = 0; i < editTotalTexts.size(); i++) {
      EditText editTotalText = editTotalTexts.get(i);
      if (TextUtils.isEmpty(editTotalText.getText().toString())) {
        editTotalText.setError(context.getString(R.string.hint_error_input));
        editTotalText.requestFocus();
        return false;
      }
      if (editPharmacyTexts.size() > 0) {
        EditText editPharmacyText = editPharmacyTexts.get(i);
        if (TextUtils.isEmpty(editPharmacyText.getText().toString())) {
          editPharmacyText.setError(context.getString(R.string.hint_error_input));
          editPharmacyText.requestFocus();
          return false;
        }
      }
    }
    return true;
  }

  public long getTotal(COUNTTYPE counttype) {
    return RnRForm.calculateTotalRegimenAmount(dataList, counttype);
  }


  public interface MMIARegimeListener {

    void loading();

    void loaded();
  }

  public void setRegimeListener(MMIARegimeListener regimeListener) {
    this.regimeListener = regimeListener;
  }
}
