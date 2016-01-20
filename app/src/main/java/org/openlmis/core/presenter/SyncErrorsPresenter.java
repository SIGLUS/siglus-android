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

    public boolean hasRnrSyncError(SyncType syncType) {
        try {
            return repository.hasSyncErrorOf(syncType);
        } catch (LMISException e) {
            e.reportToFabric();
            return false;
        }
    }
}
