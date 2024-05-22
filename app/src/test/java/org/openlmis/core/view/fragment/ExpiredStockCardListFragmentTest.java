package org.openlmis.core.view.fragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.ExpiredStockCardListPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.ExpiredStockCardListAdapter;

@RunWith(LMISTestRunner.class)
public class ExpiredStockCardListFragmentTest {
    private ExpiredStockCardListFragment fragment = new ExpiredStockCardListFragment();

    @Test
    public void shouldReturnExpiredStockCardListAdapterWhenCreateAdapterIsCalled() {
        // action
        fragment.createAdapter();
        // verification
        assertTrue(fragment.mAdapter instanceof ExpiredStockCardListAdapter);
    }

    @Test
    public void shouldReturnExpiredStockCardListPresenterWhenInitPresenterIsCalled() {
        ExpiredStockCardListPresenter mockedPresenter = mock(ExpiredStockCardListPresenter.class);
        fragment.presenter = mockedPresenter;
        // action
        Presenter actualPresenter = fragment.initPresenter();
        // verification
        assertEquals(mockedPresenter, actualPresenter);
    }

    @Test
    public void shouldCallLoadExpiredStockCardsWhenLoadStockCardsIsCalled() {
        ExpiredStockCardListPresenter mockedPresenter = mock(ExpiredStockCardListPresenter.class);
        doNothing().when(mockedPresenter).loadExpiredStockCards();

        fragment.presenter = mockedPresenter;
        // action
        fragment.loadStockCards();
        // verification
        verify(mockedPresenter).loadExpiredStockCards();
    }

    @Test
    public void shouldReturnTrueWhenIsFastScrollEnabledIsCalled() {
        assertTrue(fragment.isFastScrollEnabled());
    }
}