package org.openlmis.core.view.widget;

import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.view.activity.DummyActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

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
    private MMIARequisitionPresenter presenter;
    private DummyActivity dummyActivity;

    @Before
    public void setUp() throws Exception {
        dummyActivity = Robolectric.setupActivity(DummyActivity.class);
        mmiaRegimeList = new MMIARegimeList(dummyActivity);
        presenter = mock(MMIARequisitionPresenter.class);
        mmiaRegimeList.presenter = presenter;
    }

    @Test
    public void shouldCallDeleteMethodWhenDialogPositive() throws Exception {
        mmiaRegimeList = spy(mmiaRegimeList);
        RegimenItem item = new RegimenItem();
        Observable<Void> value = Observable.create(new Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                subscriber.onCompleted();
            }
        });
        when(presenter.deleteRegimeItem(item)).thenReturn(value);

        mmiaRegimeList.showDelConfirmDialog(item);

        SimpleDialogFragment del_confirm_dialog = (SimpleDialogFragment) dummyActivity.getFragmentManager().findFragmentByTag("del_confirm_dialog");
        SimpleDialogFragment.MsgDialogCallBack mListener = del_confirm_dialog.getMListener();
        mListener.positiveClick("");

        verify(presenter).deleteRegimeItem(item);
        verify(mmiaRegimeList).refreshRegimeView();
    }

    @Test
    public void shouldNotShowTheDelIconWhenTheFormIsAuthorised() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);

        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        regimenItems.add(regimenItem);
        rnRForm.setRegimenItemListWrapper(regimenItems);

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(RuntimeEnvironment.application), presenter);

        assertNull(mmiaRegimeList.getChildAt(1).findViewById(R.id.image_view_del));
    }

    @Test
    public void shouldShowTheCustomRegimenWhenTheFormIsMissedAndNotAuthorised() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_custom_regimen, true);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_allow_custom_regimen_in_missed_mmia_form, true);
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);


        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        regimenItems.add(regimenItem);
        rnRForm.setRegimenItemListWrapper(regimenItems);

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(RuntimeEnvironment.application), presenter);

        assertEquals(4, mmiaRegimeList.getChildCount());
    }

    @Test
    public void shouldNotShowTheCustomRegimenWhenTheFormIsMissedAndNotAuthorisedWhenToggleOff() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_custom_regimen, true);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_allow_custom_regimen_in_missed_mmia_form, false);
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);


        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        regimenItems.add(regimenItem);
        rnRForm.setRegimenItemListWrapper(regimenItems);

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(RuntimeEnvironment.application), presenter);

        assertEquals(2, mmiaRegimeList.getChildCount());
    }

    @Test
    public void shouldShowTheDelIconWhenTheFormIsNotAuthorised() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT);
        rnRForm.setRegimenItemListWrapper(newArrayList(generateRegimenItem()));

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(LMISTestApp.getContext()), presenter);

        assertThat(mmiaRegimeList.getChildAt(1).findViewById(R.id.image_view_del).getVisibility(), is(View.VISIBLE));
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