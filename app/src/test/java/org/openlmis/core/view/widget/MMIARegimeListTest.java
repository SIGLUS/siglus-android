package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.robolectric.Robolectric;

import rx.Observable;
import rx.Subscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIARegimeListTest {

    private MMIARegimeList mmiaRegimeList;
    private MMIARequisitionActivity activity;
    private MMIARequisitionPresenter presenter;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(MMIARequisitionActivity.class).create().get();
        mmiaRegimeList = new MMIARegimeList(activity);
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
        SimpleDialogFragment del_confirm_dialog = (SimpleDialogFragment) activity.getFragmentManager().findFragmentByTag("del_confirm_dialog");
        SimpleDialogFragment.MsgDialogCallBack mListener = del_confirm_dialog.getMListener();
        mListener.positiveClick("");

        verify(presenter).deleteRegimeItem(item);
        verify(mmiaRegimeList).refreshRegimeView();
    }

}