package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.widget.SyncDateBottomSheet;

public class SyncErrorsPresenter extends Presenter {

    private SyncDateBottomSheet view;

    @Inject
    SyncErrorsRepository repository;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        this.view = (SyncDateBottomSheet) v;
    }

    public boolean hasRnrSyncError() {
        return hasSyncError(SyncType.RnRForm);
    }

    public boolean hasStockCardSyncError() {
        return hasSyncError(SyncType.StockCards);
    }

    private boolean hasSyncError(SyncType syncType) {
        try {
            return repository.hasSyncErrorOf(syncType);
        } catch (LMISException e) {
            new LMISException(e,"SyncErrorsPresenter.hasSyncError").reportToFabric();
            return false;
        }
    }
}
