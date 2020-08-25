package org.openlmis.core.view.widget;

import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class MMIARegimeListTest {

    private MMIARegimeList mmiaRegimeList;
    private MMIARegimeListWrap mmiaRegimeListWrap;
    private MMIARequisitionPresenter presenter;
    private DumpFragmentActivity dummyActivity;
    private TextView totalView;
    private TextView totalPharmacy;
    private TextView tvTotalPharmacyTitle;

    @Before
    public void setUp() throws Exception {
        dummyActivity = Robolectric.setupActivity(DumpFragmentActivity.class);
        mmiaRegimeList = new MMIARegimeList(dummyActivity);
        mmiaRegimeListWrap = new MMIARegimeListWrap(dummyActivity);
        mmiaRegimeListWrap.addView(mmiaRegimeList);
        totalView = new TextView(dummyActivity);
        totalPharmacy = new TextView(dummyActivity);
        tvTotalPharmacyTitle = new TextView(dummyActivity);
        presenter = mock(MMIARequisitionPresenter.class);
        mmiaRegimeList.presenter = presenter;
    }

    @Test
    public void shouldCallDeleteMethodWhenDialogPositive() {
        mmiaRegimeList = spy(mmiaRegimeList);
        RegimenItem item = new RegimenItem();
        Observable<Void> value = Observable.create(subscriber -> subscriber.onCompleted());
        when(presenter.deleteRegimeItem(item)).thenReturn(value);

        mmiaRegimeList.showDelConfirmDialog(item);

        SimpleDialogFragment del_confirm_dialog = (SimpleDialogFragment) dummyActivity.getFragmentManager().findFragmentByTag("del_confirm_dialog");
        SimpleDialogFragment.MsgDialogCallBack mListener = del_confirm_dialog.getMListener();
        mListener.positiveClick("");

        verify(presenter).deleteRegimeItem(item);
        verify(mmiaRegimeList).refreshRegimeView();
    }

    @Test
    public void shouldNotShowTheDelIconWhenTheFormIsAuthorised() {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);

        rnRForm.setRegimenItemListWrapper(getRegimeTypeList());
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(totalView, totalPharmacy, tvTotalPharmacyTitle, presenter);

        assertNull(mmiaRegimeList.getChildAt(1).findViewById(R.id.image_view_del));
    }

    @Test
    public void shouldShowTheCustomRegimenWhenTheFormIsMissedAndNotAuthorised() {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);

        rnRForm.setRegimenItemListWrapper(getRegimeTypeList());
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(totalView, totalPharmacy, tvTotalPharmacyTitle, presenter);
        assertEquals(6, mmiaRegimeList.getChildCount());
    }


    private List<RegimenItem> getRegimeTypeList() {
        List<RegimenItem> lists = new ArrayList<>();
        Regimen regimen = new Regimen();
        regimen.setName("Adult Regimen 0");
        regimen.setDisplayOrder(0l);
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        lists.add(regimenItem);

        Regimen regimen1 = new Regimen();
        regimen1.setName("Adult Regimen 1");
        regimen1.setDisplayOrder(1l);
        regimen1.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem1 = new RegimenItem();
        regimenItem1.setRegimen(regimen1);
        lists.add(regimenItem1);

        Regimen regimen2 = new Regimen();
        regimen2.setName("Adult Regimen 2");
        regimen2.setDisplayOrder(2l);
        regimen2.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem2 = new RegimenItem();
        regimenItem2.setRegimen(regimen2);
        lists.add(regimenItem2);

        Regimen regimen3 = new Regimen();
        regimen3.setName("Adult Regimen 3");
        regimen3.setDisplayOrder(3l);
        regimen3.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem3 = new RegimenItem();
        regimenItem1.setRegimen(regimen3);
        lists.add(regimenItem3);


        Regimen regimen4 = new Regimen();
        regimen4.setName("Adult Regimen 4");
        regimen4.setDisplayOrder(4l);
        regimen4.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem4 = new RegimenItem();
        regimenItem4.setRegimen(regimen4);
        lists.add(regimenItem4);


        Regimen regimen5 = new Regimen();
        regimen5.setName("Adult Regimen 5");
        regimen5.setDisplayOrder(5l);
        regimen5.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem5 = new RegimenItem();
        regimenItem1.setRegimen(regimen5);
        lists.add(regimenItem5);


        Regimen regimen6 = new Regimen();
        regimen6.setName("Children Regimen 0");
        regimen6.setDisplayOrder(6l);
        regimen5.setType(Regimen.RegimeType.Paediatrics);
        RegimenItem regimenItem6 = new RegimenItem();
        regimenItem1.setRegimen(regimen6);
        lists.add(regimenItem6);

        Regimen regimen7 = new Regimen();
        regimen7.setName("Children Regimen 1");
        regimen7.setDisplayOrder(7l);
        regimen7.setType(Regimen.RegimeType.Paediatrics);
        RegimenItem regimenItem7 = new RegimenItem();
        regimenItem1.setRegimen(regimen7);
        lists.add(regimenItem7);

        return lists;

    }

    private List<RegimenItemThreeLines> getRegimeItemThreeLines() {
        List<RegimenItemThreeLines> list = new ArrayList<>();
        RegimenItemThreeLines firtLine = new RegimenItemThreeLines();
        firtLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_1stline));
        list.add(firtLine);
        RegimenItemThreeLines secondLine = new RegimenItemThreeLines();
        secondLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_2ndline));
        list.add(secondLine);
        RegimenItemThreeLines thirdLine = new RegimenItemThreeLines();
        thirdLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_3rdline));
        list.add(thirdLine);
        return list;
    }

    @Test
    public void shouldShowTheDelIconWhenTheFormIsNotAuthorised() {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT);
        rnRForm.setRegimenItemListWrapper(newArrayList(generateRegimenItem()));
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(totalView, totalPharmacy, tvTotalPharmacyTitle, presenter);
        assertThat(mmiaRegimeList.getChildAt(1).getVisibility(), is(View.VISIBLE));
        assertThat(((TextView) mmiaRegimeList.getChildAt(1)).getText(), is("+ Adult regime"));
    }

    private RegimenItem generateRegimenItem() {
        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        regimen.setName("customName");
        regimen.setCustom(true);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        return regimenItem;
    }
}